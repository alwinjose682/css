package io.alw.css.tradeconsumer.confirmation.model.jpa;

import io.alw.css.confirmation.MatchStatus;
import io.alw.css.domain.common.SentOrRecd;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "CONFIRMATION_MATCH_STATUS", schema = "CSS")
public class ConfirmationMatchStatusEntity {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "confMatchStatusSeq")
    @SequenceGenerator(sequenceName = "conf_match_status_seq", allocationSize = 50, name = "confMatchStatusSeq")
    Long id;

    @Column(name = "CONF_REQUEST_ID", nullable = false)
    Long confRequestId;

    @Column(name = "CONTRA_PAIR_REQ_ID")
    Long contraPairReqId;

    @Column(name = "TRADE_ID", nullable = false)
    Long tradeId;

    @Column(name = "TRADE_VERSION", nullable = false)
    Integer tradeVersion;

    @Column(name = "TRADE_LEG_ID", nullable = false)
    Long tradeLegId;

    @Column(name = "TRADE_LEG_VERSION", nullable = false)
    Integer tradeLegVersion;

    @Column(name = "MATCH_EVENT_ID")
    Long matchEventId;

    @Column(name = "MATCH_EVENT_VERSION")
    Integer matchEventVersion;

    // The confirmations could be matched on a different nostroId than the one selected by CSS
    @Column(name = "NOSTRO_ID", nullable = false)
    String nostroId;

    // The confirmations could be matched on a different ssiId than the one selected by CSS
    @Column(name = "SSI_ID", nullable = false)
    String ssiId;

    @Column(name = "SENT_OR_RECD", nullable = false)
    @Enumerated(EnumType.STRING)
    SentOrRecd sentOrRecd;

    @Column(name = "MATCH_STATUS")
    @Enumerated(EnumType.STRING)
    MatchStatus matchStatus;

    @Column(name = "MATCH_DATE")
    LocalDate matchDate;

    @Column(name = "INPUT_DATE_TIME", nullable = false)
    LocalDateTime inputDateTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmationMatchStatusEntity that = (ConfirmationMatchStatusEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getMatchEventId() {
        return matchEventId;
    }

    public void setMatchEventId(Long matchEventId) {
        this.matchEventId = matchEventId;
    }

    public Integer getMatchEventVersion() {
        return matchEventVersion;
    }

    public void setMatchEventVersion(Integer matchEventVersion) {
        this.matchEventVersion = matchEventVersion;
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

    public SentOrRecd getSentOrRecd() {
        return sentOrRecd;
    }

    public void setSentOrRecd(SentOrRecd sentOrRecd) {
        this.sentOrRecd = sentOrRecd;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public LocalDateTime getInputDateTime() {
        return inputDateTime;
    }

    public void setInputDateTime(LocalDateTime inputDateTime) {
        this.inputDateTime = inputDateTime;
    }

    public Long getConfRequestId() {
        return confRequestId;
    }

    public void setConfRequestId(Long confRequestId) {
        this.confRequestId = confRequestId;
    }

    public Long getContraPairReqId() {
        return contraPairReqId;
    }

    public void setContraPairReqId(Long contraPairReqId) {
        this.contraPairReqId = contraPairReqId;
    }
}
