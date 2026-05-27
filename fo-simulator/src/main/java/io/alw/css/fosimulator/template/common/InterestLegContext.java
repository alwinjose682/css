package io.alw.css.fosimulator.template.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class InterestLegContext {
    private BigDecimal lastUsedInterestAmount;
    private final BigDecimal lastUsedPrincipalAmount;
    private final LocalDate lastUsedPrincipalValueDate;
    private final LocalDate lastUsedMaturityValueDate;

    public InterestLegContext(BigDecimal lastUsedInterestAmount, BigDecimal lastUsedPrincipalAmount, LocalDate lastUsedPrincipalValueDate, LocalDate lastUsedMaturityValueDate) {
        this.lastUsedInterestAmount = lastUsedInterestAmount;
        this.lastUsedPrincipalAmount = lastUsedPrincipalAmount;
        this.lastUsedPrincipalValueDate = lastUsedPrincipalValueDate;
        this.lastUsedMaturityValueDate = lastUsedMaturityValueDate;
    }

    public BigDecimal lastUsedInterestAmount() {
        return lastUsedInterestAmount;
    }

    public void setLastUsedInterestAmount(BigDecimal lastUsedInterestAmount) {
        this.lastUsedInterestAmount = lastUsedInterestAmount;
    }

    public BigDecimal lastUsedPrincipalAmount() {
        return lastUsedPrincipalAmount;
    }

    public LocalDate lastUsedPrincipalValueDate() {
        return lastUsedPrincipalValueDate;
    }

    public LocalDate lastUsedMaturityValueDate() {
        return lastUsedMaturityValueDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InterestLegContext) obj;
        return Objects.equals(this.lastUsedInterestAmount, that.lastUsedInterestAmount) &&
                Objects.equals(this.lastUsedPrincipalAmount, that.lastUsedPrincipalAmount) &&
                Objects.equals(this.lastUsedPrincipalValueDate, that.lastUsedPrincipalValueDate) &&
                Objects.equals(this.lastUsedMaturityValueDate, that.lastUsedMaturityValueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastUsedInterestAmount, lastUsedPrincipalAmount, lastUsedPrincipalValueDate, lastUsedMaturityValueDate);
    }

    @Override
    public String toString() {
        return "InterestLegContext[" +
                "lastUsedInterestAmount=" + lastUsedInterestAmount + ", " +
                "lastUsedPrincipalAmount=" + lastUsedPrincipalAmount + ", " +
                "lastUsedPrincipalValueDate=" + lastUsedPrincipalValueDate + ", " +
                "lastUsedMaturityValueDate=" + lastUsedMaturityValueDate + ']';
    }

}
