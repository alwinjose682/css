package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record EntityEntityPK(
        @Column(name = "ENTITY_CODE", nullable = false)
        String entityCode,

        @Column(name = "ENTITY_VERSION", nullable = false, length = 10)
        Integer entityVersion
) {
}
