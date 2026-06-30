package io.alw.css.dbshared.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TXRW extends TXRWBase {
    private static final Logger log = LoggerFactory.getLogger(TXRW.class);

    public TXRW(PlatformTransactionManager platformTransactionManager) {
        super(platformTransactionManager);
    }

    @SafeVarargs
    @Override
    public final <T> T execute(Supplier<T> action, Class<? extends Exception>... rollbackFor) throws TransactionException {
        TransactionCallback<T> txCallback = executeAction(action, rollbackFor);
        return super.execute(txCallback);
    }

    @SafeVarargs
    @Override
    public final void executeWithoutResult(Runnable action, Class<? extends Exception>... rollbackFor) throws TransactionException {
        Consumer<TransactionStatus> txStatusConsumer = executeAction(action, rollbackFor);
        super.executeWithoutResult(txStatusConsumer);
    }

    @SafeVarargs
    @Override
    public final <T> T execute(String ctxId, Supplier<T> action, Class<? extends Exception>... rollbackFor) throws TransactionException {
        TransactionCallback<T> txCallback = executeAction(action, rollbackFor);
        return super.execute(ctxId, txCallback);
    }

    @SafeVarargs
    @Override
    public final void executeWithoutResult(String ctxId, Runnable action, Class<? extends Exception>... rollbackFor) throws TransactionException {
        Consumer<TransactionStatus> txStatusConsumer = executeAction(action, rollbackFor);
        super.executeWithoutResult(ctxId, txStatusConsumer);
    }

    private Consumer<TransactionStatus> executeAction(Runnable action, Class<? extends Exception>[] rollbackFor) {
        return ts -> {
            try {
                action.run();
            } catch (Exception e) {

                // Roll back the transaction if the exception occurred match any of the explicitly given types
                for (Class<? extends Exception> aClass : rollbackFor) {
                    if (aClass.isInstance(e)) {
                        ts.setRollbackOnly();
                        log.error("Encountered a Checked Exception during transaction. The transaction will be rolled back. Exception: " + e.getMessage());
                        throw e;
                    }
                }

                // Do not roll back if the exception occurred does not match the explicitly given types
                throw e;
            }
        };
    }

    private <T> TransactionCallback<T> executeAction(Supplier<T> action, Class<? extends Exception>[] rollbackFor) {
        return ts -> {
            try {
                return action.get();
            } catch (Exception e) {

                // Roll back the transaction if the exception occurred match any of the explicitly given types
                for (Class<? extends Exception> aClass : rollbackFor) {
                    if (aClass.isInstance(e)) {
                        ts.setRollbackOnly();
                        log.error("Encountered a Checked Exception during transaction. The transaction will be rolled back. Exception: " + e.getMessage());
                        throw e;
                    }
                }

                // Do not roll back if the exception occurred does not match the explicitly given types
                throw e;
            }
        };
    }
}
