package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;

/// NOTE: The cutOffTime value of other currencies is determined relative to the Deutschland UTC time, ie UTC+1/CET
public class CurrencyCache implements Serializable {
    public static class Key implements Serializable {
        @AffinityKeyMapped
        private String CURRCODE;

        public Key(String currCode) {
            this.CURRCODE = currCode;
        }

        public String getCurrCode() {
            return CURRCODE;
        }

        public void setCurrCode(String currCode) {
            this.CURRCODE = currCode;
        }
    }

    private String currCode;
    private String countryCode;
    private boolean pmFlag;
    private Time cutOffTime;
    private boolean active;
    private Timestamp entryTime;

    public CurrencyCache(
            String currCode,
            String countryCode,
            boolean pmFlag, // precious metal flag
            Time cutOffTime,
            boolean active,
            Timestamp entryTime
    ) {
        this.currCode = currCode;
        this.countryCode = countryCode;
        this.pmFlag = pmFlag;
        this.cutOffTime = cutOffTime;
        this.active = active;
        this.entryTime = entryTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String currCode() {
        return currCode;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public Time getCutOffTime() {
        return cutOffTime;
    }

    public void setCutOffTime(Time cutOffTime) {
        this.cutOffTime = cutOffTime;
    }

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public boolean isPmFlag() {
        return pmFlag;
    }

    public void setPmFlag(boolean pmFlag) {
        this.pmFlag = pmFlag;
    }
}
