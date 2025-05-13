package io.alw.css.refdataloader.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record BookEntityPK(
        @Column(name = "BOOK_CODE", nullable = false)
        String bookCode,

        @Column(name = "BOOK_VERSION", nullable = false, length = 10)
        Integer bookVersion
) {
}
