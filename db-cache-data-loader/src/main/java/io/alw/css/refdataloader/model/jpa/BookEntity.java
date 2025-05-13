package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOOK", schema = "css_refdata")
public non-sealed class BookEntity implements ReferenceDataJpaEntity {
    @EmbeddedId
    BookEntityPK bookEntityPK;

    @Column(name = "BOOK_NAME")
    String bookName;

    @Column(name = "ENTITY_CODE", nullable = false)
    String entityCode;

    @Column(name = "ENTITY_VERSION", nullable = false, length = 10)
    Integer entityVersion;

    @Column(name = "PRODUCT_LINE", nullable = false)
    String productLine; // I intend to simply use the TradeType as productLine name

    @Column(name = "DIVISION")
    String division;

    @Column(name = "SUPER_DIVISION")
    String superDivision;

    @Column(name = "MAIN_CLUSTER") // cluster is a reserved keyword in oracle
    String cluster;

    @Column(name = "SUB_CLUSTER")
    String subCluster;

    @Column(name = "TRADE_GROUP")
    String tradeGroup;

    @Column(name = "ACTIVE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo active;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "ENTITY_CODE", referencedColumnName = "ENTITY_CODE", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "ENTITY_VERSION", nullable = false, insertable = false, updatable = false)})
    EntityEntity entity;

    public YesNo getActive() {
        return active;
    }

    public void setActive(YesNo active) {
        this.active = active;
    }

    public BookEntityPK getBookEntityPK() {
        return bookEntityPK;
    }

    public void setBookEntityPK(BookEntityPK bookEntityPK) {
        this.bookEntityPK = bookEntityPK;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
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

    public EntityEntity getEntity() {
        return entity;
    }

    public void setEntity(EntityEntity entity) {
        this.entity = entity;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public Integer getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(Integer entityVersion) {
        this.entityVersion = entityVersion;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
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
