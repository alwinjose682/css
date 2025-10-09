package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "COUNTERPARTY", schema = "css_refdata")
public non-sealed class CounterpartyEntity implements ReferenceDataJpaEntity {

    @EmbeddedId
    CounterpartyEntityPK counterpartyEntityPK;

    @Column(name = "COUNTERPARTY_NAME")
    String counterpartyName;

    @Column(name = "PARENT", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo parent;

    @Column(name = "INTERNAL", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo internal;

    @Column(name = "PARENT_COUNTERPARTY_CODE")
    String parentCounterpartyCode;

    @Column(name = "COUNTERPARTY_TYPE")
    String counterpartyType;

    @Column(name = "BIC_CODE")
    String bicCode;

    @Column(name = "ENTITY_CODE")
    String entityCode; // non-null only if `internal`=true/ This column is entityCode and not entityID. Not a FK

    @Column(name = "ADDRESS_LINE1")
    String addressLine1;

    @Column(name = "ADDRESS_LINE2")
    String addressLine2;

    @Column(name = "CITY")
    String city;

    @Column(name = "STATE")
    String state;

    @Column(name = "COUNTRY")
    String country;

    @Column(name = "REGION")
    String region;

    @Column(name = "ACTIVE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo active;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

    // Associations
    @OneToMany(mappedBy = "counterparty", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<SsiEntity> ssiList;

    @OneToMany(mappedBy = "counterparty", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<CounterpartyNettingProfileEntity> cpNettingProfiles;

    @OneToMany(mappedBy = "counterparty", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<CounterpartySlaMappingEntity> slaMappingEntities;

    public YesNo getActive() {
        return active;
    }

    public void setActive(YesNo active) {
        this.active = active;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getBicCode() {
        return bicCode;
    }

    public void setBicCode(String bicCode) {
        this.bicCode = bicCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public CounterpartyEntityPK getCounterpartyEntityPK() {
        return counterpartyEntityPK;
    }

    public void setCounterpartyEntityPK(CounterpartyEntityPK counterpartyEntityPK) {
        this.counterpartyEntityPK = counterpartyEntityPK;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public String getCounterpartyType() {
        return counterpartyType;
    }

    public void setCounterpartyType(String counterpartyType) {
        this.counterpartyType = counterpartyType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<CounterpartyNettingProfileEntity> getCpNettingProfiles() {
        return cpNettingProfiles;
    }

    public void setCpNettingProfiles(List<CounterpartyNettingProfileEntity> cpNettingProfiles) {
        this.cpNettingProfiles = cpNettingProfiles;
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

    public YesNo getInternal() {
        return internal;
    }

    public void setInternal(YesNo internal) {
        this.internal = internal;
    }

    public YesNo getParent() {
        return parent;
    }

    public void setParent(YesNo parent) {
        this.parent = parent;
    }

    public String getParentCounterpartyCode() {
        return parentCounterpartyCode;
    }

    public void setParentCounterpartyCode(String parentCounterpartyCode) {
        this.parentCounterpartyCode = parentCounterpartyCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<CounterpartySlaMappingEntity> getSlaMappingEntities() {
        return slaMappingEntities;
    }

    public void setSlaMappingEntities(List<CounterpartySlaMappingEntity> slaMappingEntities) {
        this.slaMappingEntities = slaMappingEntities;
    }

    public List<SsiEntity> getSsiList() {
        return ssiList;
    }

    public void setSsiList(List<SsiEntity> ssiList) {
        this.ssiList = ssiList;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
