package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SSI", schema = "css_refdata")
public non-sealed class SsiEntity implements ReferenceDataJpaEntity {
    @EmbeddedId
    SsiEntityPK ssiEntityPK;

    @Column(name = "COUNTERPARTY_CODE", nullable = false)
    String counterpartyCode;

    @Column(name = "COUNTERPARTY_VERSION", nullable = false, length = 10)
    Integer counterpartyVersion;

    @Column(name = "CURR_CODE", nullable = false, length = 3)
    String currCode;

    @Column(name = "PRODUCT", nullable = false)
    String product; // Product and TradeType are treated as same

    @Column(name = "IS_PRIMARY", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo primary; // There is only one primary SSI, all others are secondary

    // Minimal delivery instruction
    @Column(name = "BENE_TYPE")
    String beneType;

    @Column(name = "BANK_BIC")
    String bankBic;

    @Column(name = "BANK_ACCOUNT")
    String bankAccount;

    @Column(name = "BANK_LINE1")
    String bankLine1;

    @Column(name = "CORR_BIC")
    String corrBic;

    @Column(name = "CORR_ACCOUNT")
    String corrAccount;

    @Column(name = "CORR_LINE1")
    String corrLine1;

    @Column(name = "ACTIVE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo active;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "COUNTERPARTY_CODE", referencedColumnName = "COUNTERPARTY_CODE", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COUNTERPARTY_VERSION", referencedColumnName = "COUNTERPARTY_VERSION", nullable = false, insertable = false, updatable = false)})
    CounterpartyEntity counterparty;

    public YesNo getActive() {
        return active;
    }

    public void setActive(YesNo active) {
        this.active = active;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankBic() {
        return bankBic;
    }

    public void setBankBic(String bankBic) {
        this.bankBic = bankBic;
    }

    public String getBankLine1() {
        return bankLine1;
    }

    public void setBankLine1(String bankLine1) {
        this.bankLine1 = bankLine1;
    }

    public String getBeneType() {
        return beneType;
    }

    public void setBeneType(String beneType) {
        this.beneType = beneType;
    }

    public String getCorrAccount() {
        return corrAccount;
    }

    public void setCorrAccount(String corrAccount) {
        this.corrAccount = corrAccount;
    }

    public String getCorrBic() {
        return corrBic;
    }

    public void setCorrBic(String corrBic) {
        this.corrBic = corrBic;
    }

    public String getCorrLine1() {
        return corrLine1;
    }

    public void setCorrLine1(String corrLine1) {
        this.corrLine1 = corrLine1;
    }

    public CounterpartyEntity getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyEntity counterparty) {
        this.counterparty = counterparty;
    }

    public String getCounterpartyCode() {
        return counterpartyCode;
    }

    public void setCounterpartyCode(String counterpartyCode) {
        this.counterpartyCode = counterpartyCode;
    }

    public Integer getCounterpartyVersion() {
        return counterpartyVersion;
    }

    public void setCounterpartyVersion(Integer counterpartyVersion) {
        this.counterpartyVersion = counterpartyVersion;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public YesNo getPrimary() {
        return primary;
    }

    public void setPrimary(YesNo primary) {
        this.primary = primary;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public SsiEntityPK getSsiEntityPK() {
        return ssiEntityPK;
    }

    public void setSsiEntityPK(SsiEntityPK ssiEntityPK) {
        this.ssiEntityPK = ssiEntityPK;
    }
}
