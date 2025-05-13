package io.alw.css.domain.exception;

public sealed interface ExceptionConstants {
    non-sealed interface ExceptionType extends ExceptionConstants {
        String TECHNICAL = "TECHNICAL";
    }

    non-sealed interface ExceptionCategory extends ExceptionConstants {
        String RECOVERABLE = "RECOVERABLE";
        String UNRECOVERABLE = "UNRECOVERABLE";
    }

    non-sealed interface ExceptionSubCategory extends ExceptionConstants {
        String INVALID_MESSAGE = "INVALID_MESSAGE";
        String INVALID_FO_VERSION = "INVALID_FO_VERSION";
        String NOT_FIRST_VERSION = "NOT_FIRST_VERSION";
        String TRADEID_MISMATCH = "TRADEID_MISMATCH";
        String INCORRECT_CF_REVISION_TYPE = "INCORRECT_CF_REVISION_TYPE";
    }
}
