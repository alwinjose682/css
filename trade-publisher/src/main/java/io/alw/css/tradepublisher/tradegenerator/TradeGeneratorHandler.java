package io.alw.css.tradepublisher.tradegenerator;

import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.tradepublisher.CssTaskExecutor;
import io.alw.css.tradepublisher.TradePublisher;
import io.alw.css.tradepublisher.model.Entity;
import io.alw.css.tradepublisher.model.GeneratorDetail;
import io.alw.css.tradepublisher.model.TradeGenerationInitialValues;
import io.alw.css.tradepublisher.model.properties.TradeGeneratorProperties;
import io.alw.css.tradepublisher.model.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.service.RefDataService;
import io.alw.css.tradepublisher.template.FxTemplate;
import io.alw.css.tradepublisher.template.IdProvider;
import io.alw.css.tradepublisher.template.MmTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.common.TradeType.*;

public final class TradeGeneratorHandler {
    private final static Logger log = LoggerFactory.getLogger(TradeGeneratorHandler.class);
    private final static String GENERATOR_KEY_PART_SEPARATOR = "-";
    private final AtomicBoolean activeHandlerOperation;
    private final Map<String, List<TradeGenerator>> generatorMap;

    private final TradeGeneratorProperties tradeGeneratorProperties;
    private final TradeTemplateProperties tradeTemplateProperties;
    private final TradePublisher tradePublisher;
    private final RefDataService refDataService;
    private final DayTicker dayTicker;
    private final CssTaskExecutor cssTaskExecutor;

    // Initial Generator Values - initialized only once
    private TradeGenerationInitialValues cfGenerationInitialValues;

    public TradeGeneratorHandler(TradeGeneratorProperties tradeGeneratorProperties, TradeTemplateProperties tradeTemplateProperties, TradePublisher tradePublisher, RefDataService refDataService, CssTaskExecutor cssTaskExecutor) {
        this.tradeGeneratorProperties = tradeGeneratorProperties;
        this.tradeTemplateProperties = tradeTemplateProperties;
        this.tradePublisher = tradePublisher;
        this.refDataService = refDataService;
        this.dayTicker = DayTicker.initSingleton(10, 30, 2, cssTaskExecutor);
        this.activeHandlerOperation = new AtomicBoolean(false);
        this.generatorMap = new ConcurrentHashMap<>();
        this.cssTaskExecutor = cssTaskExecutor;
    }

    private boolean beginHandlerOperation() {
        return activeHandlerOperation.compareAndSet(false, true);
    }

    private boolean endHandlerOperation() {
        return activeHandlerOperation.compareAndSet(true, false);
    }

    public TradeGeneratorHandlerOutcome startAllGenerators(TradeGenerationInitialValues tradeGenerationInitialValues) {
        setTradeGenerationInitialValues(tradeGenerationInitialValues);
        return startAllGenerators();
    }

    /// Sets trade generation initial values IF they are not set already.
    /// Trade generation initial values can be provided explicitly via the REST API. If not provided explicitly, then default values are used
    private void setTradeGenerationInitialValues(TradeGenerationInitialValues initValues) {
        if (this.cfGenerationInitialValues == null) {
            synchronized (this) {
                if (this.cfGenerationInitialValues == null && initValues == null) {
                    this.cfGenerationInitialValues = TradeGenerationInitialValues.defaultValues();
                    log.info("Initial values for trade generation are not provided explicitly. Starting trade generation with default initial values: {}", this.cfGenerationInitialValues);
                } else {
                    var valueDate = initValues.valueDate();
                    var tradeId = initValues.tradeId();
                    this.cfGenerationInitialValues = new TradeGenerationInitialValues(valueDate, tradeId);
                    log.info("Initial values for trade generation are provided explicitly via REST API. Starting trade generation with the explicit initial values: {}", this.cfGenerationInitialValues);
                }
                //Initialize the singleton instance of IdProvider
                IdProvider.init(this.cfGenerationInitialValues.tradeId());
            }
        }
    }

    /// First, starts the day ticker. Day ticker is started only once even if this method is invoked multiple times
    /// Second, starts one generator of each kind.
    /// Additional generators need to be started explicitly
    /// Atomic boolean is used instead of just making this method synchronized because:
    /// concurrent invocations of this method that are blocked should not attempt to start the generators again.
    /// But sequential invocations of this method will start another instance of all the generators, which is considered ok
    public TradeGeneratorHandlerOutcome startAllGenerators() {
        setTradeGenerationInitialValues(cfGenerationInitialValues);

        boolean ok = beginHandlerOperation();
        if (!ok) {
            return new TradeGeneratorHandlerOutcome.ConcurrentOperation("Another TradeGeneratorHandler operation is in progress");
        }

        // If already started, calling this method has no effect
        dayTicker.start();

        List<GeneratorDetail> startedGenerators = new ArrayList<>();
        List<TradeType> listOfSupportedTradeTypes = List.of(FX, MM_TERM, MM_CALL);

        // Create and start generator for all combinations of each TransactionType, TradeType and Entity
        for (TransactionType transactionType : TransactionType.values()) {
            for (TradeType tradeType : listOfSupportedTradeTypes) {
                for (Entity entity : refDataService.entities()) {
                    String key = getKey(transactionType, tradeType, entity);
                    final long generatorSleepDurationSeconds = getGeneratorSleepDurationFor(key);

                    try {
                        // Create the Trade supplier
                        Supplier<Set<Trade>> trdSupplier = createTradeSupplier(transactionType, tradeType, entity);
                        // Create the tradeGenerator
                        GeneratorDetail generatorDetail = new GeneratorDetail(key, generatorSleepDurationSeconds);
                        TradeGenerator tradeGenerator = createGenerator(generatorDetail, trdSupplier, tradePublisher);
                        log.info("Created Trade Generator for TransactionType: " + transactionType + ", TradeType: " + tradeType + ", Entity: " + entity + " [key: " + generatorDetail.generatorKey() + ", freq: " + generatorDetail.generationFrequency() + "]");
                        // Start the tradeGenerator
                        cssTaskExecutor.submit(tradeGenerator);
                        startedGenerators.add(generatorDetail);
                    } catch (Exception e) {
                        startedGenerators.stream().map(GeneratorDetail::generatorKey).forEach(this::stop);
                        dayTicker.stop();
                        return new TradeGeneratorHandlerOutcome.Failure(e.getMessage(), startedGenerators.stream().map(GeneratorDetail::generatorKey).toList(), List.of(key));
                    }
                }
            }
        }

        // End handler operation
        endHandlerOperation(); //TODO: What to do if not ok

        return new TradeGeneratorHandlerOutcome.Success("Successfully started all trade generators", startedGenerators);
    }

    private long getGeneratorSleepDurationFor(String generatorKey) {
        Map<String, Long> cfgProps = tradeGeneratorProperties.frequencySeconds();
        String[] gkp = generatorKey.split(GENERATOR_KEY_PART_SEPARATOR);

        for (String key : cfgProps.keySet()) {
            if (key.equalsIgnoreCase(gkp[0] + GENERATOR_KEY_PART_SEPARATOR + gkp[1])
                    || key.equalsIgnoreCase(gkp[0] + GENERATOR_KEY_PART_SEPARATOR)
                    || key.equalsIgnoreCase(GENERATOR_KEY_PART_SEPARATOR + gkp[1])) {
                return cfgProps.get(key);
            }
        }
        return tradeGeneratorProperties.frequencySecondsDefault();
    }

    public List<TradeGeneratorHandlerOutcome> stopAllGenerators() {
        List<TradeGeneratorHandlerOutcome> outcome = generatorMap.keySet().stream().map(this::stop).toList();
        dayTicker.stop();
        return outcome;
    }

    /// Creates a new generator and adds to the list of the same type of generators.
    /// Concurrent Safe. Performs this computation atomically. generatorMap is ConcurrentHashMap
    /// This method does NOT change the 'begin' or 'end' handler operation state
    private TradeGenerator createGenerator(GeneratorDetail generatorDetail, Supplier<Set<Trade>> trdSupplier, TradePublisher tradePublisher) {
        TradeGenerator newGenerator = new TradeGenerator(generatorDetail, trdSupplier, tradePublisher);
        generatorMap.compute(generatorDetail.generatorKey(), (k, v) -> {
            if (v == null) {
                List<TradeGenerator> generators = new ArrayList<>();
                generators.add(newGenerator);
                return generators;
            } else {
                v.add(newGenerator);
                return v;
            }
        });
        return newGenerator;
    }

    private Supplier<Set<Trade>> createTradeSupplier(TransactionType transactionType, TradeType tradeType, Entity entity) {
        LocalDate initialValueDate = cfGenerationInitialValues.valueDate();
        RandomGenerator rndm = RandomGenerator.getDefault();
        return switch (tradeType) {
            case FX -> new FxTemplate(entity, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);
            case MM_TERM -> new MmTemplate(entity, MM_TERM, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);
            case MM_CALL -> new MmTemplate(entity, MM_CALL, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);
            default -> throw new IllegalStateException("Trade generation not implemented yet for TradeType: " + tradeType);
        };
    }

    /// 1. Signals the [TradeGenerator] to stop in a new Thread
    /// 2. if the
    /// This method is Concurrent Safe. Performs this computation atomically.
    /// TODO: dayTicker is not stopped if all the generators are stopped in an adhoc manner
    private TradeGeneratorHandlerOutcome stop(String key) {
        TradeGeneratorHandlerOutcome[] outcome = new TradeGeneratorHandlerOutcome[1];

        // Performs this computation atomically. generatorMap is ConcurrentHashMap
        generatorMap.compute(key, (_, generators) -> {
            if (generators == null) {
                outcome[0] = new TradeGeneratorHandlerOutcome.GenericMessage("No handler with given name exists or incorrect Trade Generator name. Key: " + key);
                return null;
            } else {
                TradeGenerator generator = generators.removeFirst();
                performStop(generator, key);
                outcome[0] = new TradeGeneratorHandlerOutcome.GenericMessage("Successfully signalled stop for generator: " + key + ". The generator is expected to stop shortly. Current remaining number of generators of the same type: " + generators.size());
                if (generators.isEmpty()) {
                    return null;
                } else {
                    return generators;
                }
            }
        });

        return outcome[0];
    }

    /// 1. Stops the generator and removes the generator from the collection of generators
    /// 2. Spawns a new thread and that waits for [TradeGenerator#isTaskExecutionCompleted] to become true for `waitTimeMinutes` minutes.
    ///     - If wait period expires and the wait condition is not met, then the spawned thread logs an error message
    private void performStop(TradeGenerator generator, String key) {
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

    private String getKey(TransactionType transactionType, TradeType tradeType, Entity entity) {
        return transactionType + GENERATOR_KEY_PART_SEPARATOR + tradeType + GENERATOR_KEY_PART_SEPARATOR + entity.entityCode();
    }
}
