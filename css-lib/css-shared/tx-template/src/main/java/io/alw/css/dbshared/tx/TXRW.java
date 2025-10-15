package io.alw.css.dbshared.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

import static io.alw.css.profiling.SimpleEventActions.beginJfrEvent;
import static io.alw.css.profiling.SimpleEventActions.endJfrEvent;

public final class TXRW {
    private final TransactionTemplate txrw;

    public TXRW(PlatformTransactionManager platformTransactionManager) {
        txrw = new TransactionTemplate(platformTransactionManager, new DefaultTransactionDefinition());
        // Explicitly setting below even though the same are the defaults
        txrw.setReadOnly(false);
        txrw.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        txrw.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        WriteEvent event = new WriteEvent();
        beginJfrEvent(event);
        T res = txrw.execute(action);
        endJfrEvent(event);
        return res;
    }

    public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
        WriteEvent event = new WriteEvent();
        beginJfrEvent(event);
        txrw.executeWithoutResult(action);
        endJfrEvent(event);
    }

    public <T> T execute(String ctxId, TransactionCallback<T> action) throws TransactionException {
        WriteEvent event = new WriteEvent();
        beginJfrEvent(event);
        T res = txrw.execute(action);
        endJfrEvent(event, e -> ((WriteEvent) e).ctxId = ctxId);
        return res;
    }

    public void executeWithoutResult(String ctxId, Consumer<TransactionStatus> action) throws TransactionException {
        WriteEvent event = new WriteEvent();
        beginJfrEvent(event);
        txrw.executeWithoutResult(action);
        endJfrEvent(event, e -> ((WriteEvent) e).ctxId = ctxId);
    }
}
