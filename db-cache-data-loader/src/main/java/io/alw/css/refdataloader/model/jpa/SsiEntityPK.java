package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record SsiEntityPK(
        @Column(name = "SSI_ID", nullable = false)
        String ssiID,

        @Column(name = "SSI_VERSION", length = 10, nullable = false)
        Integer ssiVersion
) {
}
