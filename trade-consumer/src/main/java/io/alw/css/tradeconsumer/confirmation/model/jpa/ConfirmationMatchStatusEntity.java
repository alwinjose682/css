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

    @Column(name = "CASHFLOW_ID", nullable = false)
    Long cashflowId;

    @Column(name = "CASHFLOW_VERSION", nullable = false)
    Integer cashflowVersion;

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

    @Override
    public String toString() {
        return "ConfirmationMatchStatusEntity{" +
                "id=" + id +
                ", cashflowId=" + cashflowId +
                ", cashflowVersion=" + cashflowVersion +
                ", matchEventId=" + matchEventId +
                ", matchEventVersion=" + matchEventVersion +
                ", nostroId='" + nostroId + '\'' +
                ", ssiId='" + ssiId + '\'' +
                ", sentOrRecd='" + sentOrRecd + '\'' +
                ", matchStatus=" + matchStatus +
                ", matchDate=" + matchDate +
                ", inputDateTime=" + inputDateTime +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCashflowId() {
        return cashflowId;
    }

    public void setCashflowId(Long cashflowId) {
        this.cashflowId = cashflowId;
    }

    public Integer getCashflowVersion() {
        return cashflowVersion;
    }

    public void setCashflowVersion(Integer cashflowVersion) {
        this.cashflowVersion = cashflowVersion;
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
}
