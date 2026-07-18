package io.alw.css.tradepublisher.confirmation.template;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.confirmation.MatchStatus;
import io.alw.css.confirmation.TradeLegMatchAttribute;
import io.alw.css.tradepublisher.IdProvider;
import io.alw.css.tradepublisher.confirmation.ConfirmationMatchEventPublisher;
import io.alw.css.tradepublisher.generator.DayTicker;
import io.alw.css.tradepublisher.store.InMemoryStore;
import io.alw.css.tradepublisher.store.StoreHelper;
import io.alw.css.tradepublisher.trade.service.RefDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class ConfirmationMatchEventTemplate implements Supplier<List<ConfirmationMatchEvent>> {
    private static final int VERSION_ONE = 1;

    private final DayTicker dayTicker;
    private final RefDataService refDataService;
    private final StoreHelper<ConfirmationMatchRequest> matchRequestStoreHelper;
    private final StoreHelper<ConfirmationMatchEvent> matchEventStoreHelper;
    private final ConfirmationMatchEventPublisher eventPublisher;
    private final LocalDate initialValueDate;
    private final RandomGenerator rndm;

    private long dayForTemplate;

    public ConfirmationMatchEventTemplate(DayTicker dayTicker, RefDataService refDataService, ConfirmationMatchEventPublisher eventPublisher, LocalDate initialValueDate, RandomGenerator rndm) {
        this.dayTicker = dayTicker;
        this.refDataService = refDataService;
        this.matchRequestStoreHelper = new StoreHelper<>(dayTicker, new InMemoryStore<>(), rndm);
        this.matchEventStoreHelper = new StoreHelper<>(dayTicker, new InMemoryStore<>(), rndm);
        this.eventPublisher = eventPublisher;
        this.initialValueDate = initialValueDate;
        this.rndm = rndm;
        this.dayForTemplate = 0L;
    }

    /// Generates [ConfirmationMatchEvent] immediately or on a future date.
    /// The events generated immediately are publisher to CSS.
    /// The match requests and event amendments queued for a future date are obtained by [io.alw.css.tradepublisher.generator.Generator] at pre-configured intervals and then published to CSS
    public void consume(ConfirmationMatchRequest matchRequest) {
        if (isMatchEventGeneratableForFutureDate(matchRequest)) {
            matchRequestStoreHelper.storeForFutureRndmRetrievalDay(matchRequest, StoreHelper.Purpose.ITEM_SPECIFIC_EVENT);
        } else {
            updateTemplateDay();
            ConfirmationMatchEvent matchEvent = generateMatchEvent(matchRequest);
            saveForFutureAmendment(matchEvent);
            eventPublisher.accept(List.of(matchEvent));
        }
    }

    @Override
    public List<ConfirmationMatchEvent> get() {
        List<ConfirmationMatchEvent> matchEvents = new ArrayList<>();

        // Update template day
        updateTemplateDay();
        // Get saved match requests for current day
        Collection<ConfirmationMatchRequest> matchRequests = matchRequestStoreHelper.remove(StoreHelper.Purpose.ITEM_SPECIFIC_EVENT, currentDayForTemplate());
        matchRequests.forEach(req -> matchEvents.add(generateMatchEvent(req)));
        // Get saved match events for amendment for current day
        Collection<ConfirmationMatchEvent> eventsForAmendment = matchEventStoreHelper.remove(StoreHelper.Purpose.AMEND, currentDayForTemplate());
        eventsForAmendment.stream().map(evt -> matchEvents.add(generateAmendedMatchEvent(evt)));
        // Store the newly generated events for future amendment
        matchEvents.forEach(this::saveForFutureAmendment);
        // Return all the generated events
        return matchEvents;
    }

    private ConfirmationMatchEvent generateAmendedMatchEvent(ConfirmationMatchEvent evt) {
        MatchStatus matchStatus = getRndmMatchStatusForAmendMatchRequest();
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = getTradeLegMatchAttributes(evt.tradeLegMatchAttributes(), matchStatus);

        return new ConfirmationMatchEvent(
                evt.eventId(),
                evt.eventVersion() + 1,
                evt.tradeId(),
                evt.tradeVersion(),
                evt.matchRequestId(),
                evt.contraPairReqId(),
                tradeLegMatchAttributes,
                evt.tradeType(),
                matchStatus,
                currentDateForTemplate()
        );
    }

    private ConfirmationMatchEvent generateMatchEvent(ConfirmationMatchRequest matchRequest) {
        long eventId = IdProvider.singleton().nextConfMatchEventId();
        MatchStatus matchStatus = getRndmMatchStatusForInitialMatchRequest();
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = getTradeLegMatchAttributes(matchRequest.tradeLegMatchAttributes(), matchStatus);

        return new ConfirmationMatchEvent(
                eventId,
                VERSION_ONE,
                matchRequest.tradeId(),
                matchRequest.tradeVersion(),
                matchRequest.requestId(),
                matchRequest.contraPairId(),
                tradeLegMatchAttributes,
                matchRequest.tradeType(),
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

    /// This method is the starting point to start a new build cycle
    /// This method ensures that the same [DayTicker#day()] is used at all points of building multiple trades in current cycle
    private void updateTemplateDay() {
        if (dayTicker.day() != currentDayForTemplate()) {
            setDayForTrdTemplate(dayTicker.day());
        }
    }

    /// Checks and save for future amendment if feasible. There is a randomness here as well
    private void saveForFutureAmendment(ConfirmationMatchEvent matchEvent) {
        boolean canSave = rndm.nextInt(1, 50) > 30
                && matchEvent.tradeLegMatchAttributes().stream().anyMatch(attr -> attr.valueDate().isAfter(currentDateForTemplate()));
        if (canSave) {
            matchEventStoreHelper.storeForFutureRndmRetrievalDay(matchEvent, StoreHelper.Purpose.AMEND);
        }
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
