package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record NostroEntityPK(
        @Column(name = "NOSTRO_ID", nullable = false)
        String nostroID,

        @Column(name = "NOSTRO_VERSION", nullable = false, length = 10)
        Integer nostroVersion
) {
}
