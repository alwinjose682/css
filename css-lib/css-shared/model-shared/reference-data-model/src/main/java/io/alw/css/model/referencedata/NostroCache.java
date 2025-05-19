package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

/// NOTE: The cutOffTime value of other currencies is determined relative to the Deutschland UTC time, ie UTC+1/CET
public class NostroCache implements Serializable {
    public static class Key implements Serializable {
        private String NOSTROID;
        @AffinityKeyMapped
        private String ENTITYCODE;

        public Key(String nostroID, String entityCode) {
            this.NOSTROID = nostroID;
            this.ENTITYCODE = entityCode;
        }

        public String getNostroID() {
            return NOSTROID;
        }

        public void setNostroID(String nostroID) {
            this.NOSTROID = nostroID;
        }

        public String getEntityCode() {
            return ENTITYCODE;
        }

        public void setEntityCode(String entityCode) {
            this.ENTITYCODE = entityCode;
        }
    }

    private String nostroID;
    private int nostroVersion;
    private String entityCode;
    private int entityVersion;
    private String currCode;
    private String secondaryLedgerAccount; // sla is NOT unique for a nostroID. sla can be changed by amending the nostro
    private boolean isPrimary; // There is only one primary nostro, all others are secondary
    private String beneBic;
    private String bankBic;
    private String bankAccount;
    private String bankLine1;
    private String corrBic;
    private String corrAccount;
    private String corrLine1;
    private Time cutOffTime; // The end time of payment release window
    private int cutInHoursOffset; // The number of hours prior to cutOffTime
    private BigDecimal paymentLimit;
    private boolean active;
    private Timestamp entryTime;

    public NostroCache(boolean active, String bankAccount, String bankBic, String bankLine1, String beneBic, String corrAccount, String corrBic, String corrLine1, String currCode, int cutInHoursOffset, Time cutOffTime, String entityCode, int entityVersion, Timestamp entryTime, String nostroID, int nostroVersion, BigDecimal paymentLimit, boolean isPrimary, String secondaryLedgerAccount) {
        this.active = active;
        this.bankAccount = bankAccount;
        this.bankBic = bankBic;
        this.bankLine1 = bankLine1;
        this.beneBic = beneBic;
        this.corrAccount = corrAccount;
        this.corrBic = corrBic;
        this.corrLine1 = corrLine1;
        this.currCode = currCode;
        this.cutInHoursOffset = cutInHoursOffset;
        this.cutOffTime = cutOffTime;
        this.entityCode = entityCode;
        this.entityVersion = entityVersion;
        this.entryTime = entryTime;
        this.nostroID = nostroID;
        this.nostroVersion = nostroVersion;
        this.paymentLimit = paymentLimit;
        this.isPrimary = isPrimary;
        this.secondaryLedgerAccount = secondaryLedgerAccount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankBic() {
        return bankBic;
    }

    public void setBankBic(String bankBic) {
        this.bankBic = bankBic;
    }

    public String getBankLine1() {
        return bankLine1;
    }

    public void setBankLine1(String bankLine1) {
        this.bankLine1 = bankLine1;
    }

    public String getBeneBic() {
        return beneBic;
    }

    public void setBeneBic(String beneBic) {
        this.beneBic = beneBic;
    }

    public String getCorrAccount() {
        return corrAccount;
    }

    public void setCorrAccount(String corrAccount) {
        this.corrAccount = corrAccount;
    }

    public String getCorrBic() {
        return corrBic;
    }

    public void setCorrBic(String corrBic) {
        this.corrBic = corrBic;
    }

    public String getCorrLine1() {
        return corrLine1;
    }

    public void setCorrLine1(String corrLine1) {
        this.corrLine1 = corrLine1;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public int getCutInHoursOffset() {
        return cutInHoursOffset;
    }

    public void setCutInHoursOffset(int cutInHoursOffset) {
        this.cutInHoursOffset = cutInHoursOffset;
    }

    public Time getCutOffTime() {
        return cutOffTime;
    }

    public void setCutOffTime(Time cutOffTime) {
        this.cutOffTime = cutOffTime;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
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

    public String nostroID() {
        return nostroID;
    }

    public String getNostroID() {
        return nostroID;
    }

    public void setNostroID(String nostroID) {
        this.nostroID = nostroID;
    }

    public int getNostroVersion() {
        return nostroVersion;
    }

    public void setNostroVersion(int nostroVersion) {
        this.nostroVersion = nostroVersion;
    }

    public BigDecimal getPaymentLimit() {
        return paymentLimit;
    }

    public void setPaymentLimit(BigDecimal paymentLimit) {
        this.paymentLimit = paymentLimit;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getSecondaryLedgerAccount() {
        return secondaryLedgerAccount;
    }

    public void setSecondaryLedgerAccount(String secondaryLedgerAccount) {
        this.secondaryLedgerAccount = secondaryLedgerAccount;
    }
}
