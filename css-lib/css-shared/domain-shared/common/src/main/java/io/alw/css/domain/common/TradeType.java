package io.alw.css.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.alw.css.domain.common.TradeEventType.*;

public enum TradeType {
    FX(commonEventTypes()), // FXSpotForward,
    MM_TERM(commonEventTypes()),
    MM_CALL(commonEventTypes()),
    PAYMENT(commonEventTypes()),
    FX_NDF(commonEventTypesWith(List.of(FIX, UN_FIX))), // FXNonDeliverableForward
    BOND(commonEventTypesWith(List.of(COUPON, MATURE))),
    REPO(commonEventTypesWith(List.of(INTEREST_ACTION, ROLL, TERMINATE))),
    MM(commonEventTypesWith(List.of(INTEREST_ACTION, ROLL, TERMINATE))),
    OPTION(commonEventTypesWith(List.of(EXERCISE, KNOCK_OUT, EXPIRE)));

    private static List<TradeEventType> commonEventTypes() {
        List<TradeEventType> list = new ArrayList<>();
        list.add(NEW_TRADE);
        list.add(AMEND);
        list.add(REBOOK);
        list.add(CANCEL);
        return Collections.unmodifiableList(list);
    }

    private static List<TradeEventType> commonEventTypesWith(List<TradeEventType> otherTypes) {
        List<TradeEventType> list = new ArrayList<>();
        list.addAll(commonEventTypes());
        list.addAll(otherTypes);
        return Collections.unmodifiableList(list);
    }

    private final List<TradeEventType> events;

    TradeType(List<TradeEventType> events) {
        this.events = events;
    }

    public List<TradeEventType> events() {
        return events;
    }
}
