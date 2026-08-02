package io.alw.css.tradepublisher.confirmation.template;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.confirmation.MatchStatus;
import io.alw.css.confirmation.TradeLegMatchAttribute;
import io.alw.css.domain.common.TradeType;
import io.alw.css.tradepublisher.IdProvider;
import io.alw.css.tradepublisher.confirmation.ConfirmationMatchEventPublisher;
import io.alw.css.tradepublisher.generator.DayTicker;
import io.alw.css.tradepublisher.store.ExtendedInMemoryStore;
import io.alw.css.tradepublisher.store.ExtendedStoreHelper;
import io.alw.css.tradepublisher.store.StoreHelper;
import io.alw.css.tradepublisher.trade.service.RefDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class ConfirmationMatchEventTemplate implements Supplier<List<ConfirmationMatchEvent>> {
    private static final int VERSION_ONE = 1;
    private static final Logger log = LoggerFactory.getLogger(ConfirmationMatchEventTemplate.class);

    private final DayTicker dayTicker;
    private final RefDataService refDataService;
    private final ExtendedStoreHelper<ConfirmationMatchRequest> matchRequestStoreHelper;
    private final ExtendedStoreHelper<ConfirmationMatchEvent> matchEventStoreHelper;
    private final ConfirmationMatchEventPublisher eventPublisher;
    private final LocalDate initialValueDate;
    private final RandomGenerator rndm;

    private long dayForTemplate;

    public ConfirmationMatchEventTemplate(DayTicker dayTicker, RefDataService refDataService, ConfirmationMatchEventPublisher eventPublisher, LocalDate initialValueDate, RandomGenerator rndm) {
        this.dayTicker = dayTicker;
        this.refDataService = refDataService;
        this.matchRequestStoreHelper = new ExtendedStoreHelper<>(dayTicker, new ExtendedInMemoryStore<>(), rndm);
        this.matchEventStoreHelper = new ExtendedStoreHelper<>(dayTicker, new ExtendedInMemoryStore<>(), rndm);
        this.eventPublisher = eventPublisher;
        this.initialValueDate = initialValueDate;
        this.rndm = rndm;
        this.dayForTemplate = 0L;
    }

    /// Generates [ConfirmationMatchEvent] immediately or on a future date.
    /// The events generated immediately are publisher to CSS.
    /// The match requests and event amendments queued for a future date are obtained by [io.alw.css.tradepublisher.generator.Generator] at pre-configured intervals and then published to CSS
    public void consume(ConfirmationMatchRequest matchRequest) {
        if (isMatchRequestAnAmendment(matchRequest)) {
            Long contraPairId = matchRequest.contraPairId();

            // Remove the previous matchRequest and matchEvent identified by contraPairId from both the stores.
            // The previous matchRequest should not be actioned upon as an amended matchRequest is received
            // contraPairId of this matchRequest corresponds to the matchRequestId of the matchRequest that was previously processed which might be present in the stores
            // Note: The previous matchRequest may be present in only one of the store or it may not be present even in both the stores.
            // It is safe to remove without checking if it exists or not.
            // The same applies to matchEvent
            boolean remResult1 = matchRequestStoreHelper.removeById(contraPairId, StoreHelper.Purpose.ITEM_SPECIFIC_EVENT);
            boolean remResult2 = matchEventStoreHelper.removeById(contraPairId, StoreHelper.Purpose.AMEND);
            if (remResult1 || remResult2) {
                log.debug("Received amended ConfirmationMatchRequest. Removed previous MatchRequest and/or MatchEvent as candidates for matching. ConfRequestId: {}, ContraPairId: {}", matchRequest.requestId(), matchRequest.contraPairId());
            }
        }

        if (isMatchEventGeneratableForFutureDate(matchRequest)) {
            saveMatchRequest(matchRequest, StoreHelper.Purpose.ITEM_SPECIFIC_EVENT);
            log.info("Saved ConfirmationMatchRequest for future match event generation. ConfMatchRequest-Id: {}, TradeType: {}, TradeId-Ver: {}-{}", matchRequest.requestId(), matchRequest.tradeType(), matchRequest.tradeId(), matchRequest.tradeVersion());
        } else {
            newBuildCycle();
            ConfirmationMatchEvent matchEvent = generateMatchEvent(matchRequest);
            saveMatchEventForFutureAmendment(matchEvent);
            log.info("Generated 1 ConfirmationMatchEvent for publishing to CSS");
            eventPublisher.accept(List.of(matchEvent));
        }
    }

    @Override
    public List<ConfirmationMatchEvent> get() {
        List<ConfirmationMatchEvent> matchEvents = new ArrayList<>();

        // Update template day
        newBuildCycle();
        // Get saved match requests for current day
        Collection<ConfirmationMatchRequest> matchRequests = matchRequestStoreHelper.remove(StoreHelper.Purpose.ITEM_SPECIFIC_EVENT, currentDayForTemplate());
        matchRequests.forEach(req -> matchEvents.add(generateMatchEvent(req)));
        // Get saved match events for amendment for current day
        Collection<ConfirmationMatchEvent> eventsForAmendment = matchEventStoreHelper.remove(StoreHelper.Purpose.AMEND, currentDayForTemplate());
        eventsForAmendment.stream().map(evt -> matchEvents.add(generateAmendedMatchEvent(evt)));
        log.info("Generated {} ConfirmationMatchEvents for publishing to CSS", matchEvents.size());
        // Store the newly generated events for future amendment
        matchEvents.forEach(this::saveMatchEventForFutureAmendment);
        // Return all the generated events
        return matchEvents;
    }

    private ConfirmationMatchEvent generateAmendedMatchEvent(ConfirmationMatchEvent evt) {
        MatchStatus matchStatus = getRndmMatchStatusForAmendMatchRequest();
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = getTradeLegMatchAttributes(evt.tradeLegMatchAttributes(), matchStatus);

        long eventId = evt.eventId();
        int eventVersion = evt.eventVersion() + 1;
        long tradeId = evt.tradeId();
        int tradeVersion = evt.tradeVersion();
        long requestId = evt.matchRequestId();
        Long contraPairReqId = evt.contraPairReqId();
        TradeType tradeType = evt.tradeType();

        log.debug("Generated ConfirmationMatchEvent[{}-{}] with MatchStatus: {} for TradeType: {}, TradeId-Ver: {}-{}, ConfRequestId: {}, ContraPairId: {}",
                eventId, eventVersion, matchStatus, tradeType, tradeId, tradeVersion, requestId, contraPairReqId);

        return new ConfirmationMatchEvent(
                eventId,
                eventVersion,
                tradeId,
                tradeVersion,
                requestId,
                contraPairReqId,
                tradeLegMatchAttributes,
                tradeType,
                matchStatus,
                currentDateForTemplate()
        );
    }

    private ConfirmationMatchEvent generateMatchEvent(ConfirmationMatchRequest matchRequest) {
        long eventId = IdProvider.singleton().nextConfMatchEventId();
        MatchStatus matchStatus = getRndmMatchStatusForInitialMatchRequest();
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = getTradeLegMatchAttributes(matchRequest.tradeLegMatchAttributes(), matchStatus);

        long tradeId = matchRequest.tradeId();
        int tradeVersion = matchRequest.tradeVersion();
        long requestId = matchRequest.requestId();
        Long contraPairId = matchRequest.contraPairId();
        TradeType tradeType = matchRequest.tradeType();
        int versionOne = VERSION_ONE;

        log.debug("Generated ConfirmationMatchEvent[{}-{}] with MatchStatus: {} for TradeType: {}, TradeId-Ver: {}-{}, ConfRequestId: {}, ContraPairId: {}",
                eventId, versionOne, matchStatus, tradeType, tradeId, tradeVersion, requestId, contraPairId);

        return new ConfirmationMatchEvent(
                eventId,
                versionOne,
                tradeId,
                tradeVersion,
                requestId,
                contraPairId,
                tradeLegMatchAttributes,
                tradeType,
                matchStatus,
                currentDateForTemplate()
        );
    }

    /// TODO: User does a force match when some attributes differ between the host and counterparty confirmations.
    /// So for MatchStatus.MANUAL_FORCE_MATCH, just select secondary nostroId or secondary ssiId
    ///
    /// Returns an immutable set of [TradeLegMatchAttribute]
    private Set<TradeLegMatchAttribute> getTradeLegMatchAttributes(Set<TradeLegMatchAttribute> tradeLegMatchAttributes, MatchStatus matchStatus) {
        return Set.copyOf(tradeLegMatchAttributes);
    }

    private MatchStatus getRndmMatchStatusForAmendMatchRequest() {
        int rndmVal = rndm.nextInt(0, 100);
        if (rndmVal >= 0 && rndmVal <= 40) {
            return MatchStatus.MATCH;
        } else if (rndmVal > 40 && rndmVal <= 50) {
            return MatchStatus.ALLEGED_MATCH;
        } else if (rndmVal > 50 && rndmVal <= 60) {
            return MatchStatus.BREAK_MATCH;
        } else if (rndmVal > 60 && rndmVal <= 70) {
            return MatchStatus.MANUAL_MATCH;
        } else if (rndmVal > 70 && rndmVal <= 80) {
            return MatchStatus.MANUAL_FORCE_MATCH;
        } else if (rndmVal > 80 && rndmVal <= 90) {
            return MatchStatus.MANUAL_FORCE_MATCH;
        } else {
            return MatchStatus.MATCH;
        }
    }

    private MatchStatus getRndmMatchStatusForInitialMatchRequest() {
        int rndmVal = rndm.nextInt(0, 100);
        if (rndmVal >= 0 && rndmVal <= 70) {
            return MatchStatus.MATCH;
        } else if (rndmVal > 70 && rndmVal <= 80) {
            return MatchStatus.ALLEGED_MATCH;
        } else if (rndmVal > 80 && rndmVal <= 90) {
            return MatchStatus.MANUAL_MATCH;
        } else if (rndmVal > 90 && rndmVal <= 100) {
            return MatchStatus.MANUAL_FORCE_MATCH;
        } else {
            return MatchStatus.MATCH;
        }
    }

    private boolean isMatchEventGeneratableForFutureDate(ConfirmationMatchRequest req) {
        return req
                .tradeLegMatchAttributes()
                .stream().anyMatch(attr -> attr
                        .valueDate()
                        .isBefore(getFutureDateForTemplateRelativeToCurrentDate(10)));
    }

    private boolean isMatchRequestAnAmendment(ConfirmationMatchRequest matchRequest) {
        return matchRequest.contraPairId() != null;
    }

    /// This method is the starting point to start a new build cycle
    /// This method ensures that the same [DayTicker#day()] is used at all points of building multiple trades in current cycle
    private void newBuildCycle() {
        if (dayTicker.day() != currentDayForTemplate()) {
            setDayForTrdTemplate(dayTicker.day());
        }
        log.info("Started new ConfirmationMatchEvent build cycle for day: {}", currentDayForTemplate());
    }

    /// Checks and save for future amendment if feasible. There is a randomness here as well
    private void saveMatchEventForFutureAmendment(ConfirmationMatchEvent matchEvent) {
        boolean canSave = rndm.nextInt(1, 50) > 30
                && matchEvent.tradeLegMatchAttributes().stream().anyMatch(attr -> attr.valueDate().isAfter(currentDateForTemplate()));
        if (canSave) {
            matchEventStoreHelper.storeForFutureRndmRetrievalDay(matchEvent, StoreHelper.Purpose.AMEND);
            log.debug("Saved ConfirmationMatchEvent for future amendment. ConfMatchEvent-Id: {}-{}, TradeType: {}, TradeId-Ver: {}-{}", matchEvent.eventId(), matchEvent.eventVersion(), matchEvent.tradeType(), matchEvent.tradeId(), matchEvent.tradeVersion());
        }
    }

    private void saveMatchRequest(ConfirmationMatchRequest matchRequest, StoreHelper.Purpose purpose) {
        matchRequestStoreHelper.storeForFutureRndmRetrievalDay(matchRequest, purpose);
    }

    // Helper methods
    void setDayForTrdTemplate(long day) {
        this.dayForTemplate = day;
    }

    long currentDayForTemplate() {
        return dayForTemplate;
    }

    LocalDate currentDateForTemplate() {
        return initialValueDate.plusDays(dayForTemplate);
    }

    LocalDate getFutureDateForTemplateRelativeToCurrentDate(long daysToAdd) {
        return initialValueDate.plusDays(dayForTemplate + daysToAdd);
    }
}
