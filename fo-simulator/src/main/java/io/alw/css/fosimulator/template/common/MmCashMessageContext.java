package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.*;

import java.math.BigDecimal;
import java.util.List;

public final class MmCashMessageContext implements MessageContext {
    private final MmType mmType;
    private final MmLeg mmLeg;
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private final CashflowIds cashflowIds;
    private final List<TradeLink> allTradeLinks;

    private FoCashMessage foCashMessage;
    private BigDecimal interestAmount;

    public MmCashMessageContext(MmType mmType, MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis, CashflowIds cashflowIds, List<TradeLink> allTradeLinks) {
        this.mmType = mmType;
        this.mmLeg = mmLeg;
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
        this.cashflowIds = cashflowIds;
        this.allTradeLinks = allTradeLinks;
    }

    @Override
    public FoCashMessage foCashMessage() {
        return foCashMessage;
    }

    @Override
    public void setFoCashMessage(FoCashMessage foCashMessage) {
        this.foCashMessage = foCashMessage;
    }

    @Override
    public <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs) {

    }

    @Override
    public <M extends MessageContext> M with(FoCashMessage msg) {

    }

    public BigDecimal interestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public MmType mmType() {
        return mmType;
    }

    public MmLeg mmLeg() {
        return mmLeg;
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

    public CashflowIds cashflowIds() {
        return cashflowIds;
    }

    public List<TradeLink> allTradeLinks() {
        return allTradeLinks;
    }
}
