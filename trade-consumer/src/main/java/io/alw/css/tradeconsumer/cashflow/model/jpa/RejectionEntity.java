package io.alw.css.tradeconsumer.cashflow.model.jpa;

import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.common.PaymentConstants;
import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "REJECTION", schema = "CSS")
public class RejectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rejectionSeq")
    @SequenceGenerator(sequenceName = "css_common_seq", allocationSize = 50, name = "rejectionSeq")
    @Column(name = "ID")
    Long id;

    @Column(name = "TRADE_ID", nullable = false)
    Long tradeId;

    @Column(name = "trade_version", nullable = false)
    Integer tradeVersion;

    @Column(name = "TRADE_LEG_ID")
    Long tradeLegId;

    @Column(name = "trade_leg_version")
    Integer tradeLegVersion;

    // Trade and Cashflow Data
    @Column(name = "TRADE_TYPE")
    String tradeType;

    @Column(name = "TRADE_LEG_TYPE")
    String tradeLegType;

    @Column(name = "VALUE_DATE")
    LocalDate valueDate;

    @Column(name = "ENTITY_CODE")
    String entityCode;

    @Column(name = "COUNTERPARTY_CODE")
    String counterpartyCode;

    @Column(name = "AMOUNT", scale = PaymentConstants.AMOUNT_SCALE)
    BigDecimal amount;

    @Column(name = "CURR_CODE")
    String currCode;

    @Column(name = "SERVICE")
    String service;

    @Column(name = "EXCEPTION_TYPE")
    String exceptionType;

    @Column(name = "EXCEPTION_CATEGORY")
    String exceptionCategory;

    @Column(name = "EXCEPTION_SUB_CATEGORY")
    String exceptionSubCategory;

    @Column(name = "MSG")
    String msg;

    @Enumerated(EnumType.STRING)
    @Column(name = "REPLAYABLE", nullable = false, length = 1)
    YesNo replayable;

    @Column(name = "NUM_OF_RETRIES")
    Integer numOfRetries;

    @Column(name = "CREATED_DATE_TIME")
    LocalDateTime createdDateTime;

    @Column(name = "INPUT_BY", nullable = false)
    @Enumerated(EnumType.STRING)
    InputBy inputBy;

    @Column(name = "UPDATED_DATE_TIME")
    LocalDateTime updatedDateTime;

    public Long getId() {
        return id;
    }

    public RejectionEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public RejectionEntity setTradeId(Long tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    public Integer getTradeVersion() {
        return tradeVersion;
    }

    public RejectionEntity setTradeVersion(Integer tradeVersion) {
        this.tradeVersion = tradeVersion;
        return this;
    }

    public Long getTradeLegId() {
        return tradeLegId;
    }

    public RejectionEntity setTradeLegId(Long tradeLegId) {
        this.tradeLegId = tradeLegId;
        return this;
    }

    public Integer getTradeLegVersion() {
        return tradeLegVersion;
    }

    public RejectionEntity setTradeLegVersion(Integer tradeLegVersion) {
        this.tradeLegVersion = tradeLegVersion;
        return this;
    }

    public String getTradeType() {
        return tradeType;
    }

    public RejectionEntity setTradeType(String tradeType) {
        this.tradeType = tradeType;
        return this;
    }

    public String getTradeLegType() {
        return tradeLegType;
    }

    public RejectionEntity setTradeLegType(String tradeLegType) {
        this.tradeLegType = tradeLegType;
        return this;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public RejectionEntity setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
        return this;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public RejectionEntity setEntityCode(String entityCode) {
        this.entityCode = entityCode;
        return this;
    }

    public String getCounterpartyCode() {
        return counterpartyCode;
    }

    public RejectionEntity setCounterpartyCode(String counterpartyCode) {
        this.counterpartyCode = counterpartyCode;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RejectionEntity setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getCurrCode() {
        return currCode;
    }

    public RejectionEntity setCurrCode(String currCode) {
        this.currCode = currCode;
        return this;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public RejectionEntity setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
        return this;
    }

    public String getExceptionCategory() {
        return exceptionCategory;
    }

    public RejectionEntity setExceptionCategory(String exceptionCategory) {
        this.exceptionCategory = exceptionCategory;
        return this;
    }

    public String getExceptionSubCategory() {
        return exceptionSubCategory;
    }

    public RejectionEntity setExceptionSubCategory(String exceptionSubCategory) {
        this.exceptionSubCategory = exceptionSubCategory;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public RejectionEntity setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public YesNo getReplayable() {
        return replayable;
    }

    public RejectionEntity setReplayable(YesNo replayable) {
        this.replayable = replayable;
        return this;
    }

    public Integer getNumOfRetries() {
        return numOfRetries;
    }

    public RejectionEntity setNumOfRetries(Integer numOfRetries) {
        this.numOfRetries = numOfRetries;
        return this;
    }

    public InputBy getInputBy() {
        return inputBy;
    }

    public RejectionEntity setInputBy(InputBy inputBy) {
        this.inputBy = inputBy;
        return this;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public RejectionEntity setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
        return this;
    }

    public LocalDateTime getUpdatedDateTime() {
        return updatedDateTime;
    }

    public RejectionEntity setUpdatedDateTime(LocalDateTime updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RejectionEntity that = (RejectionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
