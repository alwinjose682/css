package io.alw.css.domain.exception;

import java.util.Optional;

public class CategorizedRuntimeException extends ManagedRuntimeException {
    private final Object exceptionRelatedData;
    private final String exceptionType;
    private final String exceptionCategory;
    private final String exceptionSubCategory;
    private final boolean replayable;
    private int numOfRetries;

    public CategorizedRuntimeException(String exceptionType, String exceptionCategory, String exceptionSubCategory, Object exceptionRelatedData, String message, Exception e) {
        super(message, e);
        this.exceptionType = exceptionType;
        this.exceptionCategory = exceptionCategory;
        this.exceptionSubCategory = exceptionSubCategory;
        this.replayable = isReplayable(exceptionCategory);
        this.exceptionRelatedData = exceptionRelatedData;
        this.numOfRetries = 0;
    }

    public CategorizedRuntimeException(String exceptionType, String exceptionCategory, String exceptionSubCategory, Object exceptionRelatedData, String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.exceptionCategory = exceptionCategory;
        this.exceptionSubCategory = exceptionSubCategory;
        this.replayable = isReplayable(exceptionCategory);
        this.exceptionRelatedData = exceptionRelatedData;
        this.numOfRetries = 0;
    }

    public CategorizedRuntimeException(String exceptionType, String exceptionCategory, String exceptionSubCategory, String message, Exception e) {
        super(message, e);
        this.exceptionType = exceptionType;
        this.exceptionCategory = exceptionCategory;
        this.exceptionSubCategory = exceptionSubCategory;
        this.replayable = isReplayable(exceptionCategory);
        this.exceptionRelatedData = null;
        this.numOfRetries = 0;
    }

    public CategorizedRuntimeException(String exceptionType, String exceptionCategory, String exceptionSubCategory, String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.exceptionCategory = exceptionCategory;
        this.exceptionSubCategory = exceptionSubCategory;
        this.replayable = isReplayable(exceptionCategory);
        this.exceptionRelatedData = null;
        this.numOfRetries = 0;
    }

    private boolean isReplayable(String exceptionCategory) {
        return !exceptionCategory.equals(ExceptionConstants.ExceptionCategory.UNRECOVERABLE);
    }

    public Optional<Object> exceptionRelatedData() {
        return exceptionRelatedData == null ? Optional.empty() : Optional.of(exceptionRelatedData);
    }

    public String exceptionType() {
        return exceptionType;
    }

    public String exceptionCategory() {
        return exceptionCategory;
    }

    public String exceptionSubCategory() {
        return exceptionSubCategory;
    }

    public boolean replayable() {
        return replayable;
    }

    public int numOfRetries() {
        return numOfRetries;
    }

    public void setNumOfRetries(int numOfRetries) {
        this.numOfRetries = numOfRetries;
    }
}
