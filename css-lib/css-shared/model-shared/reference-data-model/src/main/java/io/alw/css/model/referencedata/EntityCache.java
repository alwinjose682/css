package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

public class EntityCache implements Serializable {
    public static class Key implements Serializable {
        @AffinityKeyMapped
        private String ENTITYCODE;

        public Key(String entityCode) {
            this.ENTITYCODE = entityCode;
        }

        public String getEntityCode() {
            return ENTITYCODE;
        }

        public void setEntityCode(String entityCode) {
            this.ENTITYCODE = entityCode;
        }
    }

    private String entityCode;
    private int entityVersion;
    private String entityName;
    private String currCode;
    private String countryCode;
    private String countryName;
    private String bicCode;
    private boolean active;
    private Timestamp entryTime;

    public EntityCache(boolean active, String bicCode, String countryCode, String countryName, String currCode, String entityCode, String entityName, int entityVersion, Timestamp entryTime) {
        this.active = active;
        this.bicCode = bicCode;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.currCode = currCode;
        this.entityCode = entityCode;
        this.entityName = entityName;
        this.entityVersion = entityVersion;
        this.entryTime = entryTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBicCode() {
        return bicCode;
    }

    public void setBicCode(String bicCode) {
        this.bicCode = bicCode;
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

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public String entityCode() {
        return entityCode;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public int getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(int entityVersion) {
        this.entityVersion = entityVersion;
    }

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }
}
