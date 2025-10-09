package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/// NOTE: The cutOffTime value of other currencies is determined relative to the Deutschland UTC time, ie UTC+1/CET
@Entity
@Table(name = "CURRENCY", schema = "css_refdata")
public non-sealed class CurrencyEntity implements ReferenceDataJpaEntity {
    @Id
    @Column(name = "CURR_CODE", nullable = false, length = 3)
    String currCode;

    @Column(name = "COUNTRY_CODE", nullable = false, length = 2)
    String countryCode;

    @Column(name = "PM_FLAG", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo pmFlag; // precious metal flag

    @Column(name = "CUT_OFF_TIME")
    LocalTime cutOffTime;

    @Column(name = "ACTIVE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo active;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

//    TODO: should this be a uni-directional association
//    @OneToOne(fetch = FetchType.EAGER, optional = false, mappedBy = "currency")
//    CountryEntity country;

    public YesNo getActive() {
        return active;
    }

    public void setActive(YesNo active) {
        this.active = active;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public LocalTime getCutOffTime() {
        return cutOffTime;
    }

    public void setCutOffTime(LocalTime cutOffTime) {
        this.cutOffTime = cutOffTime;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public YesNo getPmFlag() {
        return pmFlag;
    }

    public void setPmFlag(YesNo pmFlag) {
        this.pmFlag = pmFlag;
    }
}
