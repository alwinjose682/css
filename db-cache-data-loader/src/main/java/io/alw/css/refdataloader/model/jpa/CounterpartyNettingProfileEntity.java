package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "COUNTERPARTY_NETTING_PROFILE", schema = "css_refdata")
public non-sealed class CounterpartyNettingProfileEntity implements ReferenceDataJpaEntity {

    @EmbeddedId
    CounterpartyNettingProfileEntityPK counterpartyNettingProfileEntityPK;

    @Column(name = "COUNTERPARTY_CODE", nullable = false)
    String counterpartyCode;

    @Column(name = "COUNTERPARTY_VERSION", nullable = false, length = 10)
    Integer counterpartyVersion;

    @Column(name = "PRODUCT", nullable = false)
    String product; // Product and TradeType are treated as same for simplicity. The enum TradeType is mapped to product field

    @Column(name = "NETTING_TYPE")
    String nettingType;

    @Column(name = "NET_BY_PARENT_COUNTERPARTY_CODE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo netByParentCounterpartyCode;

    @Column(name = "NET_FOR_ANY_ENTITY", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo netForAnyEntity;

    @Column(name = "ENTITY_CODE")
    String entityCode; // if 'netForAnyEntity' is 'N'/false, then 'entityCode' must have a value

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

    public CounterpartyNettingProfileEntityPK getCounterpartyNettingProfileEntityPK() {
        return counterpartyNettingProfileEntityPK;
    }

    public void setCounterpartyNettingProfileEntityPK(CounterpartyNettingProfileEntityPK counterpartyNettingProfileEntityPK) {
        this.counterpartyNettingProfileEntityPK = counterpartyNettingProfileEntityPK;
    }

    public Integer getCounterpartyVersion() {
        return counterpartyVersion;
    }

    public void setCounterpartyVersion(Integer counterpartyVersion) {
        this.counterpartyVersion = counterpartyVersion;
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

    public YesNo getNetByParentCounterpartyCode() {
        return netByParentCounterpartyCode;
    }

    public void setNetByParentCounterpartyCode(YesNo netByParentCounterpartyCode) {
        this.netByParentCounterpartyCode = netByParentCounterpartyCode;
    }

    public YesNo getNetForAnyEntity() {
        return netForAnyEntity;
    }

    public void setNetForAnyEntity(YesNo netForAnyEntity) {
        this.netForAnyEntity = netForAnyEntity;
    }

    public String getNettingType() {
        return nettingType;
    }

    public void setNettingType(String nettingType) {
        this.nettingType = nettingType;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
