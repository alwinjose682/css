package io.alw.css.tradeconsumer.cashflow.model.jpa;

import io.alw.css.domain.common.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CASHFLOW", schema = "CSS")
public class CashflowEntity {

    // CSS Cashflow Version Data
    @EmbeddedId
    CashflowEntityPK cashflowEntityPK;

    @Column(name = "LATEST", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo latest; // The latest field is intended to be used only by CSS Services that synchronizes Cashflow processing by acquiring a lock

    @Column(name = "REVISION_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    RevisionType revisionType;

    @Column(name = "TRADE_ID", nullable = false)
    Long tradeId;

    @Column(name = "TRADE_VERSION", nullable = false)
    Integer tradeVersion;

    @Column(name = "TRADE_LEG_ID", nullable = false)
    Long tradeLegId;

    @Column(name = "TRADE_LEG_VERSION", nullable = false)
    Integer tradeLegVersion;

    // Trade and Cashflow Data
    @Column(name = "TRADE_TYPE")
    String tradeType;

    @Column(name = "TRADE_LEG_TYPE")
    String tradeLegType;

    @Column(name = "BOOK_CODE")
    String bookCode;

    @Column(name = "COUNTER_BOOK_CODE")
    String counterBookCode;

    @Column(name = "TRANSACTION_TYPE")
    String transactionType;

    @Column(name = "RATE", scale = PaymentConstants.RATE_SCALE)
    BigDecimal rate;

    @Column(name = "VALUE_DATE")
    LocalDate valueDate;

    // ObligationData
    @Column(name = "ENTITY_CODE")
    String entityCode;

    @Column(name = "COUNTERPARTY_CODE")
    String counterpartyCode;

    @Column(name = "AMOUNT", scale = PaymentConstants.AMOUNT_SCALE)
    BigDecimal amount;

    @Column(name = "CURR_CODE")
    String currCode;

    // EnrichmentData
    @Column(name = "INTERNAL", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo internal;

    @Column(name = "NOSTRO_ID")
    String nostroId;

    @Column(name = "SSI_ID")
    String ssiId;

    @Column(name = "CONFIRMATION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    CashflowConfirmationStatus confirmationStatus;

    @Column(name = "PAYMENT_SUPPRESSION_CATEGORY")
    @Enumerated(EnumType.STRING)
    PaymentSuppressionCategory paymentSuppressionCategory;

    // Cashflow Entry Audit
    @Column(name = "INPUT_BY", nullable = false)
    @Enumerated(EnumType.STRING)
    InputBy inputBy;

    @Column(name = "INPUT_BY_USER_ID")
    String inputByUserId;

    @Column(name = "INPUT_DATE_TIME")
    LocalDateTime inputDateTime;

    public CashflowEntityPK getCashflowEntityPK() {
        return cashflowEntityPK;
    }

    public void setCashflowEntityPK(CashflowEntityPK cashflowEntityPK) {
        this.cashflowEntityPK = cashflowEntityPK;
    }

    public YesNo getLatest() {
        return latest;
    }

    public void setLatest(YesNo latest) {
        this.latest = latest;
    }

    public RevisionType getRevisionType() {
        return revisionType;
    }

    public void setRevisionType(RevisionType revisionType) {
        this.revisionType = revisionType;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getTradeVersion() {
        return tradeVersion;
    }

    public void setTradeVersion(Integer tradeVersion) {
        this.tradeVersion = tradeVersion;
    }

    public Long getTradeLegId() {
        return tradeLegId;
    }

    public void setTradeLegId(Long tradeLegId) {
        this.tradeLegId = tradeLegId;
    }

    public Integer getTradeLegVersion() {
        return tradeLegVersion;
    }

    public void setTradeLegVersion(Integer tradeLegVersion) {
        this.tradeLegVersion = tradeLegVersion;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeLegType() {
        return tradeLegType;
    }

    public void setTradeLegType(String tradeLegType) {
        this.tradeLegType = tradeLegType;
    }

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public String getCounterBookCode() {
        return counterBookCode;
    }

    public void setCounterBookCode(String counterBookCode) {
        this.counterBookCode = counterBookCode;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public String getCounterpartyCode() {
        return counterpartyCode;
    }

    public void setCounterpartyCode(String counterpartyCode) {
        this.counterpartyCode = counterpartyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public YesNo getInternal() {
        return internal;
    }

    public void setInternal(YesNo internal) {
        this.internal = internal;
    }

    public String getNostroId() {
        return nostroId;
    }

    public void setNostroId(String nostroId) {
        this.nostroId = nostroId;
    }

    public String getSsiId() {
        return ssiId;
    }

    public void setSsiId(String ssiId) {
        this.ssiId = ssiId;
    }

    public PaymentSuppressionCategory getPaymentSuppressionCategory() {
        return paymentSuppressionCategory;
    }

    public void setPaymentSuppressionCategory(PaymentSuppressionCategory paymentSuppressionCategory) {
        this.paymentSuppressionCategory = paymentSuppressionCategory;
    }

    public InputBy getInputBy() {
        return inputBy;
    }

    public void setInputBy(InputBy inputBy) {
        this.inputBy = inputBy;
    }

    public String getInputByUserId() {
        return inputByUserId;
    }

    public void setInputByUserId(String inputByUserId) {
        this.inputByUserId = inputByUserId;
    }

    public LocalDateTime getInputDateTime() {
        return inputDateTime;
    }

    public void setInputDateTime(LocalDateTime inputDateTime) {
        this.inputDateTime = inputDateTime;
    }

    public CashflowConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(CashflowConfirmationStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }
}
