package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CounterpartyNettingProfileEntityPK(

        @Column(name = "NETTING_PROFILE_ID", nullable = false, length = 19)
        Long nettingProfileID,

        @Column(name = "NETTING_PROFILE_VERSION", nullable = false, length = 10)
        Integer nettingProfileVersion
) {
}
