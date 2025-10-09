package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

///NOTE: The cutOffTime value of other currencies is determined relative to the Deutschland UTC time, ie UTC+1/CET
@Entity
@Table(name = "NOSTRO", schema = "css_refdata")
public non-sealed class NostroEntity implements ReferenceDataJpaEntity {
    @EmbeddedId
    NostroEntityPK nostroEntityPK;

    @Column(name = "ENTITY_CODE", nullable = false)
    String entityCode;

    @Column(name = "ENTITY_VERSION", nullable = false, length = 10)
    Integer entityVersion;

    @Column(name = "CURR_CODE", nullable = false)
    String currCode;

    @Column(name = "SECONDARY_LEDGER_ACCOUNT", nullable = false)
    String secondaryLedgerAccount; // sla must unique for each nostroID

    @Column(name = "IS_PRIMARY", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo primary; // There is only one primary nostro, all others are secondary

    // Minimal delivery instruction
    @Column(name = "BENE_BIC")
    String beneBic;

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

    // Payment release window
    @Column(name = "CUT_OFF_TIME")
    LocalTime cutOffTime; // The end time of payment release window

    @Column(name = "CUT_IN_HOURS_OFFSET", length = 10)
    Integer cutInHoursOffset; // The number of hours prior to cutOffTime

    @Column(name = "PAYMENT_LIMIT", scale = 5)
    BigDecimal paymentLimit;

    @Column(name = "ACTIVE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo active;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "ENTITY_CODE", referencedColumnName = "ENTITY_CODE", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "ENTITY_VERSION", nullable = false, insertable = false, updatable = false)})
    EntityEntity entity;

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

    public String getBeneBic() {
        return beneBic;
    }

    public void setBeneBic(String beneBic) {
        this.beneBic = beneBic;
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

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public Integer getCutInHoursOffset() {
        return cutInHoursOffset;
    }

    public void setCutInHoursOffset(Integer cutInHoursOffset) {
        this.cutInHoursOffset = cutInHoursOffset;
    }

    public LocalTime getCutOffTime() {
        return cutOffTime;
    }

    public void setCutOffTime(LocalTime cutOffTime) {
        this.cutOffTime = cutOffTime;
    }

    public EntityEntity getEntity() {
        return entity;
    }

    public void setEntity(EntityEntity entity) {
        this.entity = entity;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public Integer getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(Integer entityVersion) {
        this.entityVersion = entityVersion;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public NostroEntityPK getNostroEntityPK() {
        return nostroEntityPK;
    }

    public void setNostroEntityPK(NostroEntityPK nostroEntityPK) {
        this.nostroEntityPK = nostroEntityPK;
    }

    public BigDecimal getPaymentLimit() {
        return paymentLimit;
    }

    public void setPaymentLimit(BigDecimal paymentLimit) {
        this.paymentLimit = paymentLimit;
    }

    public YesNo getPrimary() {
        return primary;
    }

    public void setPrimary(YesNo primary) {
        this.primary = primary;
    }

    public String getSecondaryLedgerAccount() {
        return secondaryLedgerAccount;
    }

    public void setSecondaryLedgerAccount(String secondaryLedgerAccount) {
        this.secondaryLedgerAccount = secondaryLedgerAccount;
    }
}
