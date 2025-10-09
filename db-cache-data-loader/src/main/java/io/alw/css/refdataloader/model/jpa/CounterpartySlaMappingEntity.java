package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "COUNTERPARTY_SLA_MAPPING", schema = "css_refdata")
public non-sealed class CounterpartySlaMappingEntity implements ReferenceDataJpaEntity {
    @EmbeddedId
    CounterpartySlaMappingEntityPK counterpartySlaMappingEntityPK;

    @Column(name = "COUNTERPARTY_CODE", nullable = false)
    String counterpartyCode;

    @Column(name = "COUNTERPARTY_VERSION", nullable = false)
    int counterpartyVersion;

    @Column(name = "ENTITY_CODE")
    String entityCode;

    @Column(name = "CURR_CODE")
    String currCode;

    @Column(name = "SECONDARY_LEDGER_ACCOUNT")
    String secondaryLedgerAccount;

    @Column(name = "ACTIVE", length = 1, nullable = false)
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

    public CounterpartySlaMappingEntityPK getCounterpartySlaMappingEntityPK() {
        return counterpartySlaMappingEntityPK;
    }

    public void setCounterpartySlaMappingEntityPK(CounterpartySlaMappingEntityPK counterpartySlaMappingEntityPK) {
        this.counterpartySlaMappingEntityPK = counterpartySlaMappingEntityPK;
    }

    public int getCounterpartyVersion() {
        return counterpartyVersion;
    }

    public void setCounterpartyVersion(int counterpartyVersion) {
        this.counterpartyVersion = counterpartyVersion;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public String getSecondaryLedgerAccount() {
        return secondaryLedgerAccount;
    }

    public void setSecondaryLedgerAccount(String secondaryLedgerAccount) {
        this.secondaryLedgerAccount = secondaryLedgerAccount;
    }
}
