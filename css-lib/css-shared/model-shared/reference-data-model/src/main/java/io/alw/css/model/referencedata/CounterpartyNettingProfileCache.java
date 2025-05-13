package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

public class CounterpartyNettingProfileCache implements Serializable {
    public static class Key implements Serializable {
        private long NETTINGPROFILEID;
        @AffinityKeyMapped
        private String COUNTERPARTYCODE;

        public Key(long nettingProfileID, String counterpartyCode) {
            this.NETTINGPROFILEID = nettingProfileID;
            this.COUNTERPARTYCODE = counterpartyCode;
        }

        public long getNettingProfileID() {
            return NETTINGPROFILEID;
        }

        public void setNettingProfileID(long nettingProfileID) {
            this.NETTINGPROFILEID = nettingProfileID;
        }

        public String getCounterpartyCode() {
            return COUNTERPARTYCODE;
        }

        public void setCounterpartyCode(String counterpartyCode) {
            this.COUNTERPARTYCODE = counterpartyCode;
        }
    }

    private long nettingProfileID;
    private int nettingProfileVersion;
    private String counterpartyCode;
    private int counterpartyVersion;
    private String product; // Product and TradeType are treated as same for simplicity
    private String nettingType;
    private boolean netByParentCounterpartyCode;
    private boolean netForAnyEntity;
    private String entityCode; // if 'netForAnyEntity' is 'N'/false, then 'entityCode' must have a value
    private boolean active;
    private Timestamp entryTime;

    public CounterpartyNettingProfileCache(boolean active, String counterpartyCode, int counterpartyVersion, String entityCode, Timestamp entryTime, boolean netByParentCounterpartyCode, boolean netForAnyEntity, long nettingProfileID, int nettingProfileVersion, String nettingType, String product) {
        this.active = active;
        this.counterpartyCode = counterpartyCode;
        this.counterpartyVersion = counterpartyVersion;
        this.entityCode = entityCode;
        this.entryTime = entryTime;
        this.netByParentCounterpartyCode = netByParentCounterpartyCode;
        this.netForAnyEntity = netForAnyEntity;
        this.nettingProfileID = nettingProfileID;
        this.nettingProfileVersion = nettingProfileVersion;
        this.nettingType = nettingType;
        this.product = product;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCounterpartyCode() {
        return counterpartyCode;
    }

    public void setCounterpartyCode(String counterpartyCode) {
        this.counterpartyCode = counterpartyCode;
    }

    public int getCounterpartyVersion() {
        return counterpartyVersion;
    }

    public void setCounterpartyVersion(int counterpartyVersion) {
        this.counterpartyVersion = counterpartyVersion;
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

    public boolean isNetByParentCounterpartyCode() {
        return netByParentCounterpartyCode;
    }

    public void setNetByParentCounterpartyCode(boolean netByParentCounterpartyCode) {
        this.netByParentCounterpartyCode = netByParentCounterpartyCode;
    }

    public boolean isNetForAnyEntity() {
        return netForAnyEntity;
    }

    public void setNetForAnyEntity(boolean netForAnyEntity) {
        this.netForAnyEntity = netForAnyEntity;
    }

    public long nettingProfileID() {
        return nettingProfileID;
    }

    public long getNettingProfileID() {
        return nettingProfileID;
    }

    public void setNettingProfileID(long nettingProfileID) {
        this.nettingProfileID = nettingProfileID;
    }

    public int getNettingProfileVersion() {
        return nettingProfileVersion;
    }

    public void setNettingProfileVersion(int nettingProfileVersion) {
        this.nettingProfileVersion = nettingProfileVersion;
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
