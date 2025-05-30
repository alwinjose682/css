package io.alw.css.domain.exception;

/// [CssException] does NOT extend java.lang.Exception. But [CategorizedRuntimeException] which wraps [CssException] DOES extend java.lang.Exception
public final class CssException {
    private final String msg;
    private final ExceptionType type;
    private final ExceptionCategory category;
    private final ExceptionSubCategory subCategory;

    private final boolean replayable;
    private final int numOfRetries;

    private CssException(String msg, ExceptionType type, ExceptionCategory category, ExceptionSubCategory subCategory) {
        this.msg = msg;
        this.type = type;
        this.category = category;
        this.subCategory = subCategory;
        this.replayable = isReplayable(category);
        this.numOfRetries = 0;
    }

    private CssException(String msg, ExceptionType type, ExceptionCategory category, ExceptionSubCategory subCategory, boolean replayable, int numOfRetries) {
        this.msg = msg;
        this.type = type;
        this.category = category;
        this.subCategory = subCategory;
        this.replayable = replayable;
        this.numOfRetries = numOfRetries;
    }

    private CssException(CssException from, boolean replayable, int numOfRetries) {
        this(from.msg, from.type, from.category, from.subCategory, replayable, numOfRetries);
    }

    public static CssException TECHNICAL_UNRECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CssException(msg, ExceptionType.TECHNICAL, ExceptionCategory.UNRECOVERABLE, exceptionSubCategory);
    }

    public static CssException TECHNICAL_RECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CssException(msg, ExceptionType.TECHNICAL, ExceptionCategory.RECOVERABLE, exceptionSubCategory);
    }

    public static CssException BUSINESS_UNRECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CssException(msg, ExceptionType.BUSINESS, ExceptionCategory.UNRECOVERABLE, exceptionSubCategory);
    }

    public static CssException BUSINESS_RECOVERABLE(String msg, ExceptionSubCategory exceptionSubCategory) {
        return new CssException(msg, ExceptionType.BUSINESS, ExceptionCategory.RECOVERABLE, exceptionSubCategory);
    }

    private boolean isReplayable(ExceptionCategory exceptionCategory) {
        return exceptionCategory == ExceptionCategory.RECOVERABLE;
    }

    /// Returns a replayable CategorizedRuntimeException with `numOfRetries` incremented by 1
    public CssException replayed() {
        return new CssException(this, true, numOfRetries + 1);
    }

    /// Returns a CategorizedRuntimeException with `numOfRetries` incremented by 1 and with the provided value of replayable
    public CssException replayed(boolean replayable) {
        return new CssException(this, replayable, numOfRetries + 1);
    }

    public CssException makeNonReplayable() {
        return new CssException(this, false, numOfRetries);
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
