package io.alw.css.tradepublisher.trade.template;

import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.*;
import io.alw.css.tradepublisher.trade.model.Entity;
import io.alw.css.tradepublisher.trade.model.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.trade.service.RefDataService;
import io.alw.css.tradepublisher.trade.template.domain.TradeLegGeneratableExtendedTrade;
import io.alw.css.tradepublisher.trade.template.domain.TradeLegGenerationSchedule;
import io.alw.css.tradepublisher.trade.tradegenerator.DayTicker;
import io.alw.datagen.template.AggregateTemplateBuilderResult;
import io.alw.datagen.template.ChildBuildDirective;
import io.alw.datagen.template.ParentBuildDirective;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public abstract sealed class TradeLegGeneratingTemplate<T extends TradeLegGeneratableExtendedTrade, TT extends TradeLegGeneratingTemplate<T, TT>>
        extends TradeAmendmentTemplate<T, TT>
        permits MmTemplate {

    public TradeLegGeneratingTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, TradeTemplateProperties trdTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, trdTemplateProps);
    }

    protected abstract List<TradeLegGenerationSchedule> getInitialTradeLegGenerationSchedules(T t);

    protected abstract TradeLegGenerationSchedule buildNextTradeLegGenerationScheduleFor(TradeLegType tradeLegType, T extTrd);

    /// Generates TradeLegs based on TradeLeg generation schedule. This method is NOT for creating initial set of TradeLegs of a brand new Trade or for creating amended TradeLegs
    protected abstract ChildBuildDirective<TradeLeg, TradeLegBuilder> generateTradeLegsFromSchedule(T extTrd, TradeLegGenerationSchedule schedule);

    private final Predicate<T> newTradeLegCreationCriteriaPrimary = trd -> trd.tradeEventType() != TradeEventType.CANCEL;

    /// Return Trades(new + amends + trdLegsFromSchedule) which can be consumed by the TradeGenerators and published to CSS
    @Override
    public List<Trade> get() {
        // Build the trades(newly created + amendments)
        AggregateTemplateBuilderResult<Trade> buildResult =
                newBuildCycle()
                        .withTradeAmendmentDirectives() // 1. First, amend the Trades(ex: due to TradeEventType.AMEND, CANCEL etc)
                        .withTradeLegGenerationDirectives() // 2. Second, create new TradeLegs(ex: due to TradeEventType.INTEREST_ACTION, ROLL etc). The new TradeLegs will be created from the amended Trade in case the Trade was amended. Else, new TradeLegs will be created based on the Trade retrieved from TradeStore
                        .withRootTemplateValues()
                        .build();

        // Store the newly created trade(ExtendedTrade) for future amendments if the selection criteria allows to do so
        // The same for amended trades(ExtendedTrade) are already done during the 'related template' build process, via a runnable in the build directive, prior to reaching this point.
        saveForFutureAmendment(getExtendedTradeOfCurrentBuildCycle());
        // Get initial TradeLeg generation schedules
        List<TradeLegGenerationSchedule> schedules = getInitialTradeLegGenerationSchedules(getExtendedTradeOfCurrentBuildCycle());
        // Store the ExtendedTrade object inorder to generate TradeLeg according to schedule
        saveForTradeLegGenerationFromSchedule(schedules, getExtendedTradeOfCurrentBuildCycle());

        // Return messages(new + amends + trdLegsFromSchedule) which can be consumed by the TradeGenerators
        var tradeGeneratorInput = new ArrayList<>(buildResult.childResults());
        tradeGeneratorInput.add(buildResult.result());
        return Collections.unmodifiableList(tradeGeneratorInput);
    }

    protected TT withTradeLegGenerationDirectives() {
        // Get trades for which new TradeLegs need to be created
        final List<T> extTrds = trdStoreHelper().retrieveTradesForCurrentDay(TradeStoreHelper.TradeRetrievalPurpose.TRD_SPECIFIC_EVENT);
        if (extTrds.isEmpty()) {
            return self();
        }

        for (T extTrd : extTrds) {
            List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> tradeLegDirectives = new ArrayList<>();
            List<TradeLegGenerationSchedule> newSchedules = new ArrayList<>();

            // Get all schedules to generate new TradeLegs
            List<TradeLegGenerationSchedule> schedules = extTrd.getTradeLegGenerationSchedulesTill(trdTemplateHelper.currentDayForTrdTemplate());
            // If Trade was saved for TradeLeg generation, it must be done with at least one schedule. If not, throw an exception
            if (schedules.isEmpty()) {
                throw new RuntimeException("Attempt to generate TradeLeg according to schedule for an ExtendedTrade retrieved from store, but no schedule is present");
            }

            // For each TradeLeg, create build directive and next schedule corresponding to the executed schedule
            for (TradeLegGenerationSchedule schedule : schedules) {
                // Create build directive for TradeLeg generation as per schedule
                var trdLegDirective = generateTradeLegsFromSchedule(extTrd, schedule);
                tradeLegDirectives.add(trdLegDirective);
                // Create new schedule
                TradeLegGenerationSchedule newSchedule = buildNextTradeLegGenerationScheduleFor(schedule.tradeLegType(), extTrd);
                newSchedules.add(newSchedule);
            }

            // Store the ExtendedTrade object inorder to generate TradeLeg according to new schedule
            saveForTradeLegGenerationFromSchedule(newSchedules, extTrd);

            // Create Build directive and register with AggregatedTemplateBuilder
            // Trade Builder function. No change to the Trade
            final Supplier<TradeBuilder> trdBdrFunc = () -> createBuilderFrom(extTrd.trade());
            // Trade and TradeLegs association function
            BiFunction<Trade, Set<TradeLeg>, Trade> tradeAndTradeLegAssociationFunc = Trade::clearAndAddTradeLegs;
            // The build directive
            var buildDirective = new ParentBuildDirective.ParentBuildDirectiveType1<>(trdBdrFunc, tradeLegDirectives, tradeAndTradeLegAssociationFunc, null);
            // 2. Register the amendmentTradeBuildDirective with the template builder
            this.withRelatedTemplateDirective(buildDirective);
        }

        return self();
    }

    /// Saves the schedules in ExtendedTrade object and saves the ExtendedTrade object in store for retrieval on the nearest scheduleDay
    private void saveForTradeLegGenerationFromSchedule(List<TradeLegGenerationSchedule> schedules, T extTrd) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        // save the schedules in ExtendedTrade object
        extTrd.addTradeLegGenerationSchedules(schedules);
        // save the ExtendedTrade object in store for retrieval on the nearest scheduleDay
        Optional<TradeLegGenerationSchedule> minSched = schedules.stream().min(Comparator.comparingLong(TradeLegGenerationSchedule::scheduleDay));
        minSched.ifPresent(sched -> {
            if (newTradeLegCreationCriteriaPrimary.test(extTrd)) {
                trdStoreHelper().storeTradeForFutureRetrievalDay(extTrd, TradeStoreHelper.TradeRetrievalPurpose.TRD_SPECIFIC_EVENT, sched.scheduleDay());
            }
        });
    }
}
