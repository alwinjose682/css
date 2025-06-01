package io.alw.css.domain.exception;

import java.time.LocalDateTime;

public class CategorizedRuntimeException extends RuntimeException {
    private final LocalDateTime createdTime;
    private final String msg;
    private final ExceptionType type;
    private final ExceptionCategory category;
    private final ExceptionSubCategory subCategory;

    private final boolean replayable;
    private final int numOfRetries;

    private CategorizedRuntimeException(String msg, ExceptionType type, ExceptionCategory category, ExceptionSubCategory subCategory) {
        this.createdTime = LocalDateTime.now();
        this.msg = msg;
        this.type = type;
        this.category = category;
        this.subCategory = subCategory;
        this.replayable = isReplayable(category);
        this.numOfRetries = 0;
    }

    private CategorizedRuntimeException(String msg, ExceptionType type, ExceptionCategory category, ExceptionSubCategory subCategory, boolean replayable, int numOfRetries) {
        this.createdTime = LocalDateTime.now();
        this.msg = msg;
        this.type = type;
        this.category = category;
        this.subCategory = subCategory;
        this.replayable = replayable;
        this.numOfRetries = numOfRetries;
    }

    private CategorizedRuntimeException(CategorizedRuntimeException from, boolean replayable, int numOfRetries) {
        this(from.msg, from.type, from.category, from.subCategory, replayable, numOfRetries);
    }

    public static CategorizedRuntimeException TECHNICAL_UNRECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CategorizedRuntimeException(msg, ExceptionType.TECHNICAL, ExceptionCategory.UNRECOVERABLE, exceptionSubCategory);
    }

    public static CategorizedRuntimeException TECHNICAL_RECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CategorizedRuntimeException(msg, ExceptionType.TECHNICAL, ExceptionCategory.RECOVERABLE, exceptionSubCategory);
    }

    public static CategorizedRuntimeException BUSINESS_UNRECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CategorizedRuntimeException(msg, ExceptionType.BUSINESS, ExceptionCategory.UNRECOVERABLE, exceptionSubCategory);
    }

    public static CategorizedRuntimeException BUSINESS_RECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CategorizedRuntimeException(msg, ExceptionType.BUSINESS, ExceptionCategory.RECOVERABLE, exceptionSubCategory);
    }

    private boolean isReplayable(ExceptionCategory exceptionCategory) {
        return exceptionCategory == ExceptionCategory.RECOVERABLE;
    }

    /// Returns a replayable CategorizedRuntimeException with `numOfRetries` incremented by 1
    public CategorizedRuntimeException replayed() {
        return new CategorizedRuntimeException(this, true, numOfRetries + 1);
    }

    /// Returns a CategorizedRuntimeException with `numOfRetries` incremented by 1 and with the provided value of replayable
    public CategorizedRuntimeException replayed(boolean replayable) {
        return new CategorizedRuntimeException(this, replayable, numOfRetries + 1);
    }

    public CategorizedRuntimeException makeNonReplayable() {
        return new CategorizedRuntimeException(this, false, numOfRetries);
    }

    public LocalDateTime createdTime() {
        return createdTime;
    }

    public boolean replayable() {
        return replayable;
    }

    public int numOfRetries() {
        return numOfRetries;
    }

    public String msg() {
        return msg;
    }
}
