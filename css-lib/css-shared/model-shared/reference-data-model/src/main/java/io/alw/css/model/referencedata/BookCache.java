package io.alw.css.model.referencedata;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.io.Serializable;
import java.sql.Timestamp;

public class BookCache implements Serializable {
    public static class Key implements Serializable {
        private String BOOKCODE;
        @AffinityKeyMapped
        private String ENTITYCODE;

        public Key(String bookCode, String entityCode) {
            this.BOOKCODE = bookCode;
            this.ENTITYCODE = entityCode;
        }

        public String getBookCode() {
            return BOOKCODE;
        }

        public void setBookCode(String bookCode) {
            this.BOOKCODE = bookCode;
        }

        public String getEntityCode() {
            return ENTITYCODE;
        }

        public void setEntityCode(String entityCode) {
            this.ENTITYCODE = entityCode;
        }
    }

    private String bookCode;
    private int bookVersion;
    private String bookName;
    private String entityCode;
    private int entityVersion;
    private String productLine; // I intend to simply use the TradeType as productLine name
    private String division;
    private String superDivision;
    private String cluster;
    private String subCluster;
    private String tradeGroup;
    private boolean active;
    private Timestamp entryTime;

    public BookCache(boolean active, String bookCode, String bookName, int bookVersion, String cluster, String division, String entityCode, int entityVersion, Timestamp entryTime, String productLine, String subCluster, String superDivision, String tradeGroup) {
        this.active = active;
        this.bookCode = bookCode;
        this.bookName = bookName;
        this.bookVersion = bookVersion;
        this.cluster = cluster;
        this.division = division;
        this.entityCode = entityCode;
        this.entityVersion = entityVersion;
        this.entryTime = entryTime;
        this.productLine = productLine;
        this.subCluster = subCluster;
        this.superDivision = superDivision;
        this.tradeGroup = tradeGroup;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String bookCode() {
        return bookCode;
    }

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getBookVersion() {
        return bookVersion;
    }

    public void setBookVersion(int bookVersion) {
        this.bookVersion = bookVersion;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
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

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }

    public String getSubCluster() {
        return subCluster;
    }

    public void setSubCluster(String subCluster) {
        this.subCluster = subCluster;
    }

    public String getSuperDivision() {
        return superDivision;
    }

    public void setSuperDivision(String superDivision) {
        this.superDivision = superDivision;
    }

    public String getTradeGroup() {
        return tradeGroup;
    }

    public void setTradeGroup(String tradeGroup) {
        this.tradeGroup = tradeGroup;
    }
}
