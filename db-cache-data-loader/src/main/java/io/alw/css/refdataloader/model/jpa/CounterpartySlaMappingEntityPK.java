package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CounterpartySlaMappingEntityPK(
        @Column(name = "MAPPING_ID", length = 19, nullable = false)
        Long mappingID,

        @Column(name = "MAPPING_VERSION", length = 10, nullable = false)
        Integer mappingVersion
) {
}
