package io.alw.css.dbshared.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.alw.css.profiling.SimpleEventActions.endJfrEvent;

sealed abstract class TXRWBase permits TXRW {
    protected final TransactionTemplate txrw;

    protected TXRWBase(PlatformTransactionManager platformTransactionManager) {
        txrw = new TransactionTemplate(platformTransactionManager, new DefaultTransactionDefinition());
        // Explicitly setting below even though the same are the defaults
        txrw.setReadOnly(false);
        txrw.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        txrw.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public abstract <T> T execute(Supplier<T> action, Class<? extends Exception>... rollbackFor) throws TransactionException;

    public abstract void executeWithoutResult(Runnable action, Class<? extends Exception>... rollbackFor) throws TransactionException;

    public abstract <T> T execute(String ctxId, Supplier<T> action, Class<? extends Exception>... rollbackFor) throws TransactionException;

    public abstract void executeWithoutResult(String ctxId, Runnable action, Class<? extends Exception>... rollbackFor) throws TransactionException;

    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        var event = WriteEvent.start();
        T res;
        try {
            res = txrw.execute(action);
        } finally {
            endJfrEvent(event);
        }
        return res;
    }

    public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
        var event = WriteEvent.start();
        try {
            txrw.executeWithoutResult(action);
        } finally {
            endJfrEvent(event);
        }
    }

    public <T> T execute(String ctxId, TransactionCallback<T> action) throws TransactionException {
        var event = WriteEvent.start();
        T res;
        try {
            res = txrw.execute(action);
        } finally {
            endJfrEvent(event, e -> ((WriteEvent) e).setCtxId(ctxId));
        }
        return res;
    }

    public void executeWithoutResult(String ctxId, Consumer<TransactionStatus> action) throws TransactionException {
        var event = WriteEvent.start();
        try {
            txrw.executeWithoutResult(action);
        } finally {
            endJfrEvent(event, e -> ((WriteEvent) e).setCtxId(ctxId));
        }
    }
}
