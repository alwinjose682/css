package io.alw.css.domain.exception;

public class CategorizedRuntimeException extends ManagedRuntimeException {
    private final CssException cssException;


    public CategorizedRuntimeException(CssException cssException) {
        super(cssException.msg());
        this.cssException = cssException;

    }
}
