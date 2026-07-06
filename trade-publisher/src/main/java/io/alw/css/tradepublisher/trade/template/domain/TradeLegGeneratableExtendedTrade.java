package io.alw.css.tradepublisher.trade.template.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public sealed interface TradeLegGeneratableExtendedTrade extends ExtendedTrade permits MmTrade {
    Map<Long, Set<TradeLegGenerationSchedule>> tradeLegGenerationSchedules();

    void addTradeLegGenerationSchedules(List<TradeLegGenerationSchedule> tradeLegGenerationSchedules);

    long lastTradeLegGenerationDay();

    void setLastTradeLegGenerationDay(long lastTradeLegGenerationDay);

    default List<TradeLegGenerationSchedule> getTradeLegGenerationSchedulesTill(long day) {
        List<TradeLegGenerationSchedule> allSchedules = new ArrayList<>();
        long lastTradeLegGenerationDay = lastTradeLegGenerationDay();
        while (lastTradeLegGenerationDay <= day) {
            Set<TradeLegGenerationSchedule> schedules = tradeLegGenerationSchedules().get(lastTradeLegGenerationDay++);
            allSchedules.addAll(schedules);
        }
        setLastTradeLegGenerationDay(lastTradeLegGenerationDay);

        return allSchedules;
    }
}
