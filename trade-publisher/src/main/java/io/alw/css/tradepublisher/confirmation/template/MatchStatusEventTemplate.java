package io.alw.css.tradepublisher.confirmation.template;

import io.alw.css.confirmation.MatchStatus;
import io.alw.css.confirmation.MatchStatusEvent;
import io.alw.css.confirmation.TradeLegMatchAttribute;
import io.alw.css.confirmation.TradeMatchRequest;
import io.alw.css.tradepublisher.IdProvider;
import io.alw.css.tradepublisher.confirmation.MatchStatusEventPublisher;
import io.alw.css.tradepublisher.generator.DayTicker;
import io.alw.css.tradepublisher.store.InMemoryItemStore;
import io.alw.css.tradepublisher.store.ItemStoreHelper;
import io.alw.css.tradepublisher.trade.service.RefDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class MatchStatusEventTemplate implements Supplier<List<MatchStatusEvent>> {
    private static final int VERSION_ONE = 1;

    private final DayTicker dayTicker;
    private final RefDataService refDataService;
    private final ItemStoreHelper<TradeMatchRequest> matchRequestStoreHelper;
    private final ItemStoreHelper<MatchStatusEvent> matchStatusEventStoreHelper;
    private final MatchStatusEventPublisher eventPublisher;
    private final LocalDate initialValueDate;
    private final RandomGenerator rndm;

    private long dayForTemplate;

    public MatchStatusEventTemplate(DayTicker dayTicker, RefDataService refDataService, MatchStatusEventPublisher eventPublisher, LocalDate initialValueDate, RandomGenerator rndm) {
        this.dayTicker = dayTicker;
        this.refDataService = refDataService;
        this.matchRequestStoreHelper = new ItemStoreHelper<>(dayTicker, new InMemoryItemStore<>(), rndm);
        this.matchStatusEventStoreHelper = new ItemStoreHelper<>(dayTicker, new InMemoryItemStore<>(), rndm);
        this.eventPublisher = eventPublisher;
        this.initialValueDate = initialValueDate;
        this.rndm = rndm;
        this.dayForTemplate = 0L;
    }

    /// Generates [MatchStatusEvent] immediately or on a future date.
    /// The events generated immediately are publisher to CSS.
    /// The match requests and event amendments queued for a future date are obtained by [io.alw.css.tradepublisher.generator.Generator] at pre-configured intervals and then published to CSS
    public void consume(TradeMatchRequest matchRequest) {
        if (isMatchStatusGeneratableForFutureDate(matchRequest)) {
            matchRequestStoreHelper.storeForFutureRndmRetrievalDay(matchRequest, ItemStoreHelper.Purpose.ITEM_SPECIFIC_EVENT);
        } else {
            updateTemplateDay();
            MatchStatusEvent matchStatusEvent = generateMatchStatus(matchRequest);
            saveForFutureAmendment(matchStatusEvent);
            eventPublisher.accept(List.of(matchStatusEvent));
        }
    }

    @Override
    public List<MatchStatusEvent> get() {
        List<MatchStatusEvent> matchStatusEvents = new ArrayList<>();

        // Update template day
        updateTemplateDay();
        // Get saved match requests for current day
        List<TradeMatchRequest> matchRequests = matchRequestStoreHelper.retrieve(ItemStoreHelper.Purpose.ITEM_SPECIFIC_EVENT, currentDayForTemplate());
        matchRequests.forEach(req -> matchStatusEvents.add(generateMatchStatus(req)));
        // Get saved match status events for amendment for current day
        List<MatchStatusEvent> eventsForAmendment = matchStatusEventStoreHelper.retrieve(ItemStoreHelper.Purpose.AMEND, currentDayForTemplate());
        eventsForAmendment.stream().map(evt -> matchStatusEvents.add(generateAmendedMatchStatus(evt)));
        // Store the newly generated events for future amendment
        matchStatusEvents.forEach(this::saveForFutureAmendment);
        // Return all the generated events
        return matchStatusEvents;
    }

    private MatchStatusEvent generateAmendedMatchStatus(MatchStatusEvent evt) {
        MatchStatus matchStatus = getRndmMatchStatusForAmendMatchRequest();
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = getTradeLegMatchAttributes(evt.tradeLegMatchAttributes(), matchStatus);

        return new MatchStatusEvent(
                evt.eventId(),
                evt.eventVersion() + 1,
                evt.tradeId(),
                evt.tradeVersion(),
                tradeLegMatchAttributes,
                evt.tradeType(),
                matchStatus,
                currentDateForTemplate()
        );
    }

    private MatchStatusEvent generateMatchStatus(TradeMatchRequest matchRequest) {
        long eventId = IdProvider.singleton().nextMatchStatusEventId();
        MatchStatus matchStatus = getRndmMatchStatusForInitialMatchRequest();
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = getTradeLegMatchAttributes(matchRequest.tradeLegMatchAttributes(), matchStatus);

        return new MatchStatusEvent(
                eventId,
                VERSION_ONE,
                matchRequest.tradeId(),
                matchRequest.tradeVersion(),
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
            return MatchStatus.NOT_MATCH;
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
            return MatchStatus.NOT_MATCH;
        } else if (rndmVal > 80 && rndmVal <= 90) {
            return MatchStatus.MANUAL_MATCH;
        } else if (rndmVal > 90 && rndmVal <= 100) {
            return MatchStatus.MANUAL_FORCE_MATCH;
        } else {
            return MatchStatus.MATCH;
        }
    }

    private boolean isMatchStatusGeneratableForFutureDate(TradeMatchRequest req) {
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
    private void saveForFutureAmendment(MatchStatusEvent matchEvent) {
        boolean canSave = rndm.nextInt(1, 50) > 30
                && matchEvent.tradeLegMatchAttributes().stream().anyMatch(attr -> attr.valueDate().isAfter(currentDateForTemplate()));
        if (canSave) {
            matchStatusEventStoreHelper.storeForFutureRndmRetrievalDay(matchEvent, ItemStoreHelper.Purpose.AMEND);
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
