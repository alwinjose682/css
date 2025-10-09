package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "COUNTRY", schema = "css_refdata")
public non-sealed class CountryEntity implements ReferenceDataJpaEntity {
    @Id
    @Column(name = "COUNTRY_CODE", nullable = false, length = 2)
    String countryCode;

    @Column(name = "COUNTRY_NAME")
    String countryName;

    @Column(name = "REGION")
    String region;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

//    TODO: should this be a uni-directional association
//    @OneToOne(fetch = FetchType.EAGER, optional = false)
//    @JoinColumn(name = "countryCode", referencedColumnName = "countryCode", nullable = false, insertable = false, updatable = false, unique = true)
//    CurrencyEntity currency;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
