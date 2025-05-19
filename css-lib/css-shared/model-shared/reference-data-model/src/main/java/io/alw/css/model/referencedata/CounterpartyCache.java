package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

public class CounterpartyCache implements Serializable {
    public static class Key implements Serializable {
        @AffinityKeyMapped
        private String COUNTERPARTYCODE;

        public Key(String counterpartyCode) {
            this.COUNTERPARTYCODE = counterpartyCode;
        }

        public String getCounterpartyCode() {
            return COUNTERPARTYCODE;
        }

        public void setCounterpartyCode(String counterpartyCode) {
            this.COUNTERPARTYCODE = counterpartyCode;
        }
    }

    private String counterpartyCode;
    private int counterpartyVersion;
    private String counterpartyName;
    private boolean parent;
    private boolean internal;
    private String parentCounterpartyCode;
    private String counterpartyType;
    private String bicCode;
    private String entityCode; // non-null only if `internal`=true
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String region;
    private boolean active;
    private Timestamp entryTime;

    public CounterpartyCache(boolean active, String addressLine1, String addressLine2, String bicCode, String city, String counterpartyCode, String counterpartyName, String counterpartyType, int counterpartyVersion, String country, String entityCode, Timestamp entryTime, boolean internal, boolean parent, String parentCounterpartyCode, String region, String state) {
        this.active = active;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.bicCode = bicCode;
        this.city = city;
        this.counterpartyCode = counterpartyCode;
        this.counterpartyName = counterpartyName;
        this.counterpartyType = counterpartyType;
        this.counterpartyVersion = counterpartyVersion;
        this.country = country;
        this.entityCode = entityCode;
        this.entryTime = entryTime;
        this.internal = internal;
        this.parent = parent;
        this.parentCounterpartyCode = parentCounterpartyCode;
        this.region = region;
        this.state = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
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

    public String counterpartyCode() {
        return counterpartyCode;
    }

    public String getCounterpartyCode() {
        return counterpartyCode;
    }

    public void setCounterpartyCode(String counterpartyCode) {
        this.counterpartyCode = counterpartyCode;
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

    public int getCounterpartyVersion() {
        return counterpartyVersion;
    }

    public void setCounterpartyVersion(int counterpartyVersion) {
        this.counterpartyVersion = counterpartyVersion;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
