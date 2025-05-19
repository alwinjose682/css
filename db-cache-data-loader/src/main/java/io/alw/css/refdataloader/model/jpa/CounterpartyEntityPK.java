package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CounterpartyEntityPK(
        @Column(name = "COUNTERPARTY_CODE", nullable = false)
        String counterpartyCode,

        @Column(name = "COUNTERPARTY_VERSION", nullable = false, length = 10)
        Integer counterpartyVersion
) {
}
