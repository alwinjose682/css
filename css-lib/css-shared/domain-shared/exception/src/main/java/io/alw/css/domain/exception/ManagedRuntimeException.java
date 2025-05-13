package io.alw.css.domain.exception;

import java.time.LocalDateTime;

public abstract class ManagedRuntimeException extends RuntimeException {
    private final LocalDateTime createdDate;

    public ManagedRuntimeException(String message, Exception e) {
        super(message, e);
        this.createdDate = LocalDateTime.now();
    }

    public ManagedRuntimeException(String message) {
        super(message);
        this.createdDate = LocalDateTime.now();
    }
}
