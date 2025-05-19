package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

public class SsiCache implements Serializable {
    public static class Key implements Serializable {
        private String SSIID;
        @AffinityKeyMapped
        private String COUNTERPARTYCODE;

        public Key(String ssiID, String counterpartyCode) {
            this.SSIID = ssiID;
            this.COUNTERPARTYCODE = counterpartyCode;
        }

        public String getSsiID() {
            return SSIID;
        }

        public void setSsiID(String ssiID) {
            this.SSIID = ssiID;
        }

        public String getCounterpartyCode() {
            return COUNTERPARTYCODE;
        }

        public void setCounterpartyCode(String counterpartyCode) {
            this.COUNTERPARTYCODE = counterpartyCode;
        }
    }

    private String ssiID;
    private int ssiVersion;
    private String counterpartyCode; // Counterparty SSI is not associated with an entity.
    private int counterpartyVersion;
    private String currCode;
    private String product; // In CSS, I am considering Cashflow#TradeType same as product (or actually product as Cashflow#TradeType?)
    private boolean isPrimary; // There is only one primary SSI, all others are secondary
    private String beneType;
    private String bankBic;
    private String bankAccount;
    private String bankLine1;
    private String corrBic;
    private String corrAccount;
    private String corrLine1;
    private boolean active;
    private Timestamp entryTime;


    public SsiCache(boolean active, String bankAccount, String bankBic, String bankLine1, String beneType, String corrAccount, String corrBic, String corrLine1, String counterpartyCode, int counterpartyVersion, String currCode, Timestamp entryTime, boolean isPrimary, String product, String ssiID, int ssiVersion) {
        this.active = active;
        this.bankAccount = bankAccount;
        this.bankBic = bankBic;
        this.bankLine1 = bankLine1;
        this.beneType = beneType;
        this.corrAccount = corrAccount;
        this.corrBic = corrBic;
        this.corrLine1 = corrLine1;
        this.counterpartyCode = counterpartyCode;
        this.counterpartyVersion = counterpartyVersion;
        this.currCode = currCode;
        this.entryTime = entryTime;
        this.isPrimary = isPrimary;
        this.product = product;
        this.ssiID = ssiID;
        this.ssiVersion = ssiVersion;
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

    public String getBeneType() {
        return beneType;
    }

    public void setBeneType(String beneType) {
        this.beneType = beneType;
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

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String ssiID() {
        return ssiID;
    }

    public String getSsiID() {
        return ssiID;
    }

    public void setSsiID(String ssiID) {
        this.ssiID = ssiID;
    }

    public int getSsiVersion() {
        return ssiVersion;
    }

    public void setSsiVersion(int ssiVersion) {
        this.ssiVersion = ssiVersion;
    }
}
