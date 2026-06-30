package io.alw.css.tradeconsumer.model.jpa;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "TRADE_LINK", schema = "CSS")
public class TradeLinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tradeLinkEntitySeq")
    @SequenceGenerator(sequenceName = "css_common_seq", allocationSize = 1, name = "tradeLinkEntitySeq")
    Long id;

    @Column(name = "TRADE_ID", nullable = false)
    Long tradeId;

    @Column(name = "TRADE_VERSION", nullable = false, length = 10)
    Integer tradeVersion;

    @Column(name = "LINK_TYPE")
    String linkType;

    @Column(name = "RELATED_REFERENCE")
    String relatedReference;

    @Column(name = "RELATED_TRADE_ID")
    Long relatedTradeId;

    @Column(name = "RELATED_TRADE_VERSION")
    Integer relatedTradeVersion;

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

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getRelatedReference() {
        return relatedReference;
    }

    public void setRelatedReference(String relatedReference) {
        this.relatedReference = relatedReference;
    }

    public Long getRelatedTradeId() {
        return relatedTradeId;
    }

    public void setRelatedTradeId(Long relatedTradeId) {
        this.relatedTradeId = relatedTradeId;
    }

    public Integer getRelatedTradeVersion() {
        return relatedTradeVersion;
    }

    public void setRelatedTradeVersion(Integer relatedTradeVersion) {
        this.relatedTradeVersion = relatedTradeVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TradeLinkEntity that = (TradeLinkEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TradeLinkEntity{" +
                "id=" + id +
                ", tradeId=" + tradeId +
                ", tradeVersion=" + tradeVersion +
                ", linkType='" + linkType + '\'' +
                ", relatedReference='" + relatedReference + '\'' +
                ", relatedTradeId=" + relatedTradeId +
                ", relatedTradeVersion=" + relatedTradeVersion +
                '}';
    }
}
