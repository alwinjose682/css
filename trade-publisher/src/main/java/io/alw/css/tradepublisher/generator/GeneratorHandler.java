package io.alw.css.tradepublisher.generator;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.tradepublisher.CssTaskExecutor;
import io.alw.css.tradepublisher.IdProvider;
import io.alw.css.tradepublisher.confirmation.ConfirmationMatchEventPublisher;
import io.alw.css.tradepublisher.confirmation.template.ConfirmationMatchEventTemplate;
import io.alw.css.tradepublisher.properties.ConfirmationMatchEventGeneratorProperties;
import io.alw.css.tradepublisher.properties.TradeGeneratorProperties;
import io.alw.css.tradepublisher.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.trade.TradePublisher;
import io.alw.css.tradepublisher.trade.model.Entity;
import io.alw.css.tradepublisher.trade.model.GeneratorDetail;
import io.alw.css.tradepublisher.trade.model.GeneratorInitialValues;
import io.alw.css.tradepublisher.trade.service.RefDataService;
import io.alw.css.tradepublisher.trade.template.FxTemplate;
import io.alw.css.tradepublisher.trade.template.MmTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.common.TradeType.*;

public final class GeneratorHandler {
    private final static Logger log = LoggerFactory.getLogger(GeneratorHandler.class);
    private final static String GENERATOR_KEY_PART_SEPARATOR = "-";
    private final AtomicBoolean activeHandlerOperation;
    private final Map<String, List<Generator<Trade>>> tradeGeneratorMap;
    private final Map<String, List<Generator<ConfirmationMatchEvent>>> matchStatusGeneratorMap;

    // Spring beans
    private final TradeGeneratorProperties tradeGeneratorProperties;
    private final ConfirmationMatchEventGeneratorProperties confirmationMatchEventGeneratorProperties;
    private final TradeTemplateProperties tradeTemplateProperties;
    private final TradePublisher tradePublisher;
    private final ConfirmationMatchEventPublisher confirmationMatchEventPublisher;
    private final RefDataService refDataService;
    private final CssTaskExecutor cssTaskExecutor;
    // Other Constants
    private final DayTicker dayTicker;
    private final RandomGenerator rndm;

    // Initial Generator Values - initialized only once(Lazy Constants)
    private GeneratorInitialValues generatorInitialValues;
    private ConfirmationMatchEventTemplate confirmationMatchEventTemplate;

    public GeneratorHandler(TradeGeneratorProperties tradeGeneratorProperties, ConfirmationMatchEventGeneratorProperties confirmationMatchEventGeneratorProperties, TradeTemplateProperties tradeTemplateProperties, TradePublisher tradePublisher, ConfirmationMatchEventPublisher confirmationMatchEventPublisher, RefDataService refDataService, CssTaskExecutor cssTaskExecutor) {
        this.tradeGeneratorProperties = tradeGeneratorProperties;
        this.confirmationMatchEventGeneratorProperties = confirmationMatchEventGeneratorProperties;
        this.tradeTemplateProperties = tradeTemplateProperties;
        this.tradePublisher = tradePublisher;
        this.confirmationMatchEventPublisher = confirmationMatchEventPublisher;
        this.refDataService = refDataService;
        this.dayTicker = DayTicker.initSingleton(10, 30, 2, cssTaskExecutor);
        this.activeHandlerOperation = new AtomicBoolean(false);
        this.tradeGeneratorMap = new ConcurrentHashMap<>();
        this.matchStatusGeneratorMap = new ConcurrentHashMap<>();
        this.cssTaskExecutor = cssTaskExecutor;
        this.rndm = RandomGenerator.getDefault();
    }

    private boolean beginHandlerOperation() {
        return activeHandlerOperation.compareAndSet(false, true);
    }

    private boolean endHandlerOperation() {
        return activeHandlerOperation.compareAndSet(true, false);
    }

    public GeneratorHandlerOutcome startAllTradeGenerators(GeneratorInitialValues generatorInitialValues) {
        setGeneratorInitialValues(generatorInitialValues);
        return startAllTradeGenerators();
    }

    /// Sets trade generation initial values IF they are not set already.
    /// Trade generation initial values can be provided explicitly via the REST API. If not provided explicitly, then default values are used
    private void setGeneratorInitialValues(GeneratorInitialValues initValues) {
        if (this.generatorInitialValues == null) {
            synchronized (this) {
                if (this.generatorInitialValues == null && initValues == null) {
                    this.generatorInitialValues = GeneratorInitialValues.defaultValues();
                    log.info("Initial values for trade generation are not provided explicitly. Starting trade generation with default initial values: {}", this.generatorInitialValues);
                } else if (this.generatorInitialValues == null) {
                    var valueDate = initValues.valueDate();
                    var tradeId = initValues.tradeId();
                    var matchStatusEventId = initValues.matchStatusEventId();
                    this.generatorInitialValues = new GeneratorInitialValues(valueDate, tradeId, matchStatusEventId);
                    log.info("Initial values for trade generation are provided explicitly via REST API. Starting trade generation with the explicit initial values: {}", this.generatorInitialValues);
                }
                // Initialize the singleton instance of IdProvider
                IdProvider.init(this.generatorInitialValues.tradeId(), this.generatorInitialValues.matchStatusEventId());
            }
        }
    }

    /// 1. starts the day ticker. Day ticker is started only once even if this method is invoked multiple times.
    /// 2. starts all generators of all types(Trade and ConfirmationMatchEvent generator) of each kind.
    ///
    /// Atomic boolean is used instead of just making this method synchronized because:
    /// concurrent invocations of this method that are blocked should not attempt to start the generators again.
    /// Sequential invocations of this method will start another instance of all the generators, which is considered ok
    ///
    /// New type of generator or just a new instance of a generator can be started explicitly using other methods.
    ///
    /// @see GeneratorHandler#startTradeGenerators()
    public GeneratorHandlerOutcome startAllTradeGenerators() {
        setGeneratorInitialValues(generatorInitialValues);

        try {
            // Ensure no concurrent handler operations can occur
            boolean ok = beginHandlerOperation();
            if (!ok) {
                return new GeneratorHandlerOutcome.ConcurrentOperation("Another TradeGeneratorHandler operation is in progress");
            }

            // If day ticker is already started, calling start method has no effect
            dayTicker.start();

            return startTradeGenerators();
        } finally {
            // End handler operation
            endHandlerOperation(); //TODO: What to do if not ok
        }
    }

    /// Creates Trade Generators and a single ConfirmationMatchEvent Generator
    ///
    /// For each combination of TransactionType, TradeType and Entity:
    /// 1. Create Trade suppliers(using Trade Templates for FX, MM etc)
    /// 2. Create Generators which invoke the Trade Supplier to generate Trades like FX, MM etc
    /// 3. Start the Generators on new Virtual Threads
    private GeneratorHandlerOutcome startTradeGenerators() {
        List<GeneratorDetail> startedGenerators = new ArrayList<>();
        List<TradeType> listOfSupportedTradeTypes = List.of(FX, MM_TERM, MM_CALL);

        // Start Trade generators for all combinations of TransactionType, TradeType and Entity
        for (TransactionType transactionType : TransactionType.values()) {
            for (TradeType tradeType : listOfSupportedTradeTypes) {
                for (Entity entity : refDataService.entities()) {
                    String key = null;
                    try {
                        key = getTradeGeneratorKey(transactionType, tradeType, entity);
                        final long generatorSleepDurationSeconds = getGeneratorSleepDurationFor(key);

                        // Create Trade Supplier
                        Supplier<List<Trade>> trdSupplier = createTradeSupplier(transactionType, tradeType, entity);

                        // Create Trade Generator
                        GeneratorDetail generatorDetail = new GeneratorDetail(key, generatorSleepDurationSeconds);
                        Generator<Trade> generator = createGenerator(generatorDetail, trdSupplier, tradePublisher, tradeGeneratorMap);
                        log.info("Created Trade Generator for TransactionType: {}, TradeType: {}, Entity: {}, [key: {}, freq: {}]", transactionType, tradeType, entity, generatorDetail.generatorKey(), generatorDetail.generationFrequency());

                        // Start the tradeGenerator
                        cssTaskExecutor.submit(generator);
                        startedGenerators.add(generatorDetail);
                    } catch (Exception e) {
                        tradeGeneratorMap.entrySet().forEach(this::stop);
                        dayTicker.stop();
                        return new GeneratorHandlerOutcome.Failure(
                                e.getMessage(),
                                startedGenerators.stream().map(GeneratorDetail::generatorKey).toList(),
                                key == null ? null : List.of(key));
                    }
                }
            }
        }

        // Start a single instance of ConfirmationMatchEvent generator
        String key = GeneratorType.CONF_MATCH_EVENT.name();
        long generatorSleepDurationSeconds = confirmationMatchEventGeneratorProperties.amendmentFrequencySeconds();
        try {
            // Create ConfirmationMatchEvent Supplier and save the reference so that the KafkaConsumer can also access the template
            if (this.confirmationMatchEventTemplate == null) {
                this.confirmationMatchEventTemplate = createConfirmationMatchEventTemplate();
            }

            // Create ConfirmationMatchEvent Generator
            GeneratorDetail generatorDetail = new GeneratorDetail(key, generatorSleepDurationSeconds);
            Generator<ConfirmationMatchEvent> generator = createGenerator(generatorDetail, confirmationMatchEventTemplate, confirmationMatchEventPublisher, matchStatusGeneratorMap);
            log.info("Created ConfirmationMatchEvent Generator [key: {}, freq: {}]", generatorDetail.generatorKey(), generatorDetail.generationFrequency());

            // Start the ConfirmationMatchEvent Generator
            cssTaskExecutor.submit(generator);
            startedGenerators.add(generatorDetail);
        } catch (Exception e) {
            // If failed, stop both the ConfirmationMatchEvent generator and the set of Trade generators
            tradeGeneratorMap.entrySet().forEach(this::stop);
            matchStatusGeneratorMap.entrySet().forEach(this::stop);
            dayTicker.stop();

            return new GeneratorHandlerOutcome.Failure(
                    e.getMessage(),
                    startedGenerators.stream().map(GeneratorDetail::generatorKey).toList(),
                    List.of(key));
        }

        return new GeneratorHandlerOutcome.Success("Successfully started all trade generators", Collections.unmodifiableList(startedGenerators));
    }

    private ConfirmationMatchEventTemplate createConfirmationMatchEventTemplate() {
        LocalDate initialValueDate = generatorInitialValues.valueDate();
        return new ConfirmationMatchEventTemplate(dayTicker, refDataService, confirmationMatchEventPublisher, initialValueDate, rndm);
    }

    private long getGeneratorSleepDurationFor(String generatorKey) {
        Map<String, Long> cfgProps = tradeGeneratorProperties.frequencySeconds();
        String[] gkp = generatorKey.split(GENERATOR_KEY_PART_SEPARATOR);
        String generatorType = gkp[0];
        String transactionType = gkp[1];
        String tradeType = gkp[2];
        String entityCode = gkp[3];

        for (String key : cfgProps.keySet()) {
            if (
                // Evaluates in the order of preference
                    key.equalsIgnoreCase(generatorType + GENERATOR_KEY_PART_SEPARATOR + transactionType + GENERATOR_KEY_PART_SEPARATOR + tradeType + GENERATOR_KEY_PART_SEPARATOR + entityCode)
                            || key.equalsIgnoreCase(transactionType + GENERATOR_KEY_PART_SEPARATOR + tradeType + GENERATOR_KEY_PART_SEPARATOR + entityCode)
                            || key.equalsIgnoreCase(transactionType + GENERATOR_KEY_PART_SEPARATOR + tradeType)
                            || key.equalsIgnoreCase(tradeType + GENERATOR_KEY_PART_SEPARATOR + entityCode)
                            || key.equalsIgnoreCase(tradeType)
                            || key.equalsIgnoreCase(entityCode)
                            || key.equalsIgnoreCase(transactionType)
            ) {
                return cfgProps.get(key);
            }
        }
        return tradeGeneratorProperties.frequencySecondsDefault();
    }

    public List<GeneratorHandlerOutcome> stopAllGenerators() {
        // Stop all generators
        List<GeneratorHandlerOutcome> outcome1 = tradeGeneratorMap.entrySet().stream().map(this::stop).toList();
        List<GeneratorHandlerOutcome> outcome2 = matchStatusGeneratorMap.entrySet().stream().map(this::stop).toList();

        // Stop day ticker
        dayTicker.stop();

        var outcome = new ArrayList<>(outcome1);
        outcome.addAll(outcome2);

        return outcome;
    }

    /// Creates a new generator and adds to the list of the same type of generators.
    /// Concurrent Safe. Performs this computation atomically. generatorMap is ConcurrentHashMap
    /// This method does NOT change the 'begin' or 'end' handler operation state
    private <T> Generator<T> createGenerator(GeneratorDetail generatorDetail, Supplier<List<T>> supplier, Consumer<List<T>> consumer, Map<String, List<Generator<T>>> generatorMap) {
        Generator<T> newGenerator = new Generator<>(generatorDetail, supplier, consumer);
        generatorMap.compute(generatorDetail.generatorKey(), (k, v) -> {
            if (v == null) {
                List<Generator<T>> generators = new ArrayList<>();
                generators.add(newGenerator);
                return generators;
            } else {
                v.add(newGenerator);
                return v;
            }
        });
        return newGenerator;
    }

    private Supplier<List<Trade>> createTradeSupplier(TransactionType transactionType, TradeType tradeType, Entity entity) {
        LocalDate initialValueDate = generatorInitialValues.valueDate();

        return switch (tradeType) {
            case FX -> new FxTemplate(entity, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);
            case MM_TERM -> new MmTemplate(entity, MM_TERM, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);
            case MM_CALL -> new MmTemplate(entity, MM_CALL, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);
            default -> throw new IllegalStateException("Trade generation not implemented yet for TradeType: " + tradeType);
        };
    }

    /// 1. Signals the [Generator] to stop in a new Thread
    /// This method is Concurrent Safe(synchronized on monitor). Hence, performs this computation atomically.
    /// TODO: dayTicker is not stopped if all the generators are stopped in an adhoc manner
    private synchronized <T> GeneratorHandlerOutcome stop(Map.Entry<String, List<Generator<T>>> mapEntry) {
        GeneratorHandlerOutcome[] outcome = new GeneratorHandlerOutcome[1];

        String key = mapEntry.getKey();
        List<Generator<T>> generators = mapEntry.getValue();
        if (generators == null) {
            outcome[0] = new GeneratorHandlerOutcome.GenericMessage("No generator with given name is currently running or incorrect generator name. GeneratorKey: " + key);
        } else {
            Generator<T> generator = generators.removeFirst();
            performStop(generator, key);
            outcome[0] = new GeneratorHandlerOutcome.GenericMessage("Successfully signalled stop for generator: " + key + ". The generator is expected to stop shortly. Current remaining number of generators of the same type: " + generators.size());
        }

        return outcome[0];
    }

    /// 1. Stops the generator and removes the generator from the collection of generators
    /// 2. Spawns a new thread and that waits for [Generator#isTaskExecutionCompleted] to become true for `waitTimeMinutes` minutes.
    ///     - If wait period expires and the wait condition is not met, then the spawned thread logs an error message
    private <T> void performStop(Generator<T> generator, String key) {
        // Signal Stop
        generator.stop();

        // Spawn a new thread that waits for the status that says there are no pending tasks for the thread and will be stopped
        cssTaskExecutor.submit(() -> {
            int waitTimeMinutes = 1;
            boolean stopped = false;
            LocalDateTime startTime = LocalDateTime.now();
            do {
                try {
                    Thread.sleep(15 * 1_000);
                } catch (InterruptedException e) { // TODO: Thread.currentThread().interrupt ?
                    throw new RuntimeException(e);
                }
                if (generator.isTaskExecutionCompleted()) {
                    stopped = true;
                    break;
                }
            } while (Duration.between(startTime, LocalDateTime.now()).toMinutes() < waitTimeMinutes);

            if (stopped) {
                log.info("Successfully stopped generator with key: {}", key);
            } else {
                log.error("The request to stop thread did not work even after 1 minute of wait time. Generator: {}", key);
            }
        });

    }

    private String getTradeGeneratorKey(TransactionType transactionType, TradeType tradeType, Entity entity) {
        return GeneratorType.TRADE.name() + GENERATOR_KEY_PART_SEPARATOR + transactionType + GENERATOR_KEY_PART_SEPARATOR + tradeType + GENERATOR_KEY_PART_SEPARATOR + entity.entityCode();
    }

    public ConfirmationMatchEventTemplate confirmationMatchEventTemplate() {
        return confirmationMatchEventTemplate;
    }
}
