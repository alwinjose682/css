package io.alw.css.domain.exception;

public record ExceptionSubCategory(
        String type,
        Object context
) {
}
