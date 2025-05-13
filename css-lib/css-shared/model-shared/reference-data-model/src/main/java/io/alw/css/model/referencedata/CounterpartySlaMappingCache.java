package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

/// Note on secondaryLedgerAccount/sla:
/// sla is NOT unique for a nostroID. sla can be changed by amending the nostro.
/// So; when default selection of nostroID is overridden due to the presence of this sla mapping; it must be verified that the nostroId selected based on sla belongs to the same entityCode and currCode defined by this mapping
///
/// Why not simply map counterparty directly to a nostroID instead of the sla?
/// The back office application I used to work for did not have such a simple mapping. Dont know about the complexities of designing and managing a reference data system
public class CounterpartySlaMappingCache implements Serializable {
    public static class Key implements Serializable {
        private long MAPPINGID;
        @AffinityKeyMapped
        private String COUNTERPARTYCODE;

        public Key(long mappingID, String counterpartyCode) {
            this.MAPPINGID = mappingID;
            this.COUNTERPARTYCODE = counterpartyCode;
        }

        public long getMappingID() {
            return MAPPINGID;
        }

        public void setMappingID(long mappingID) {
            this.MAPPINGID = mappingID;
        }

        public String getCounterpartyCode() {
            return COUNTERPARTYCODE;
        }

        public void setCounterpartyCode(String counterpartyCode) {
            this.COUNTERPARTYCODE = counterpartyCode;
        }
    }

    private long mappingID;
    private int mappingVersion;
    private String counterpartyCode;
    private int counterpartyVersion;
    private String entityCode;
    private String currCode;
    private String secondaryLedgerAccount;
    private boolean active;
    private Timestamp entryTime;

    public CounterpartySlaMappingCache(boolean active, String counterpartyCode, int counterpartyVersion, String currCode, String entityCode, Timestamp entryTime, long mappingID, int mappingVersion, String secondaryLedgerAccount) {
        this.active = active;
        this.counterpartyCode = counterpartyCode;
        this.counterpartyVersion = counterpartyVersion;
        this.currCode = currCode;
        this.entityCode = entityCode;
        this.entryTime = entryTime;
        this.mappingID = mappingID;
        this.mappingVersion = mappingVersion;
        this.secondaryLedgerAccount = secondaryLedgerAccount;
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

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public long mappingID() {
        return mappingID;
    }

    public long getMappingID() {
        return mappingID;
    }

    public void setMappingID(long mappingID) {
        this.mappingID = mappingID;
    }

    public int getMappingVersion() {
        return mappingVersion;
    }

    public void setMappingVersion(int mappingVersion) {
        this.mappingVersion = mappingVersion;
    }

    public String getSecondaryLedgerAccount() {
        return secondaryLedgerAccount;
    }

    public void setSecondaryLedgerAccount(String secondaryLedgerAccount) {
        this.secondaryLedgerAccount = secondaryLedgerAccount;
    }
}
