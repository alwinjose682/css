package io.alw.css.tradepublisher.trade.template.domain;

import io.alw.css.domain.common.RateType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public final class MmTrade implements TradeLegGeneratableExtendedTrade {
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private Trade trade;

    // TradeLeg generation based on schedule
    private final Map<Long, Set<TradeLegGenerationSchedule>> trdLegGenerationSchedules;
    private long lastTradeLegGenerationDay;

    private int nextTradeLegId;
    private TradeLeg principalLeg;
    private TradeLeg maturityLeg;
    private final List<InterestTradeLeg> interestLegs;

    public MmTrade(RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis, long lastTradeLegGenerationDay) {
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
        this.interestLegs = new ArrayList<>();
        this.nextTradeLegId = resetTradeLegIdProvider();
        this.trdLegGenerationSchedules = new HashMap<>();
        this.lastTradeLegGenerationDay = lastTradeLegGenerationDay;
    }


    @Override
    public TradeLeg rootTradeLeg() {
        return principalLeg;
    }

    @Override
    public void setRootTradeLeg(TradeLeg rootTradeLeg) {
        this.principalLeg = rootTradeLeg;
    }

    @Override
    public long nextTradeLegId() {
        return ++nextTradeLegId;
    }

    @Override
    public int resetTradeLegIdProvider() {
        nextTradeLegId = 0;
        return nextTradeLegId;
    }

    @Override
    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    public TradeLeg principalLeg() {
        return principalLeg;
    }

    public TradeLeg maturityLeg() {
        return maturityLeg;
    }

    public void setMaturityLeg(TradeLeg maturityLeg) {
        this.maturityLeg = maturityLeg;
    }

    public void addInterestLeg(TradeDetail trdLeg) {
        if (trdLeg instanceof InterestTradeLeg intTrdLeg) {
            interestLegs.add(intTrdLeg);
        } else {
            throw new RuntimeException("An InterestTradeLeg is expected instead of TradeLeg");
        }
    }

    public List<InterestTradeLeg> interestLegs() {
        return interestLegs;
    }

    public RateType rateType() {
        return rateType;
    }

    public InterestPayoutFrequency ipFrequency() {
        return ipFrequency;
    }

    public InterestBasis interestBasis() {
        return interestBasis;
    }

    @Override
    public Trade trade() {
        return trade;
    }

    @Override
    public Iterable<TradeLeg> allTradeLegs() {
        var allTradeLegs = new ArrayList<TradeLeg>();
        allTradeLegs.add(principalLeg);
        allTradeLegs.add(maturityLeg);
        allTradeLegs.addAll(interestLegs.stream().map(InterestTradeLeg::interestLeg).toList());
        return allTradeLegs;
    }

    @Override
    public TradeLeg getTradeLegFrom(TradeDetail tradeDetail) {
        if (tradeDetail instanceof InterestTradeLeg itl) {
            return itl.interestLeg();
        } else {
            return (TradeLeg) tradeDetail;
        }
    }

    @Override
    public Map<Long, Set<TradeLegGenerationSchedule>> tradeLegGenerationSchedules() {
        return trdLegGenerationSchedules;
    }

    @Override
    public void addTradeLegGenerationSchedules(List<TradeLegGenerationSchedule> tradeLegGenerationSchedules) {
        tradeLegGenerationSchedules.stream()
                .collect(Collectors.groupingBy(TradeLegGenerationSchedule::scheduleDay, toSet()))
                .forEach((day, newScheduleSet) -> this.trdLegGenerationSchedules.computeIfAbsent(day, _ -> new HashSet<>()).addAll(newScheduleSet));
    }

    @Override
    public long lastTradeLegGenerationDay() {
        return lastTradeLegGenerationDay;
    }

    @Override
    public void setLastTradeLegGenerationDay(long lastTradeLegGenerationDay) {
        this.lastTradeLegGenerationDay = lastTradeLegGenerationDay;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MmTrade mmTrade = (MmTrade) o;
        return Objects.equals(trade, mmTrade.trade);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(trade);
    }
}
