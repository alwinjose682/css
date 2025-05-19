package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

public class CountryCache implements Serializable {
    public static class Key implements Serializable {
        @AffinityKeyMapped
        private String COUNTRYCODE;

        public Key(String countryCode) {
            this.COUNTRYCODE = countryCode;
        }

        public String getCountryCode() {
            return COUNTRYCODE;
        }

        public void setCountryCode(String countryCode) {
            this.COUNTRYCODE = countryCode;
        }
    }

    private String countryCode;
    private String countryName;
    private String region;
    private Timestamp entryTime;

    public CountryCache(String countryCode, String countryName, String region, Timestamp entryTime) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.entryTime = entryTime;
        this.region = region;
    }

    public String countryCode() {
        return countryCode;
    }

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

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
