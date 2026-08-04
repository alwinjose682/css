package io.alw.css.tradeconsumer.confirmation.exception;

public class CashflowConfirmationException extends RuntimeException {
    private final int numOfErrors;

    public CashflowConfirmationException(String msg, int numOfErrors) {
        super(msg);
        this.numOfErrors = numOfErrors;
    }

    public int numOfErrors() {
        return numOfErrors;
    }
}
