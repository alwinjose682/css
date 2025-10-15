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

public final class TXRO {
    private final TransactionTemplate txro;

    public TXRO(PlatformTransactionManager platformTransactionManager) {
        txro = new TransactionTemplate(platformTransactionManager, new DefaultTransactionDefinition());
        txro.setReadOnly(true); // overridden the default
        // Explicitly setting below even though the same are the defaults
        txro.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        txro.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        ReadEvent event = new ReadEvent();
        beginJfrEvent(event);
        var res = txro.execute(action);
        endJfrEvent(event);
        return res;
    }

    public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
        ReadEvent event = new ReadEvent();
        beginJfrEvent(event);
        txro.executeWithoutResult(action);
        endJfrEvent(event);
    }

    public <T> T execute(String ctxId, TransactionCallback<T> action) throws TransactionException {
        ReadEvent event = new ReadEvent();
        beginJfrEvent(event);
        var res = txro.execute(action);
        endJfrEvent(event, e -> ((ReadEvent) e).ctxId = ctxId);
        return res;
    }

    public void executeWithoutResult(String ctxId, Consumer<TransactionStatus> action) throws TransactionException {
        ReadEvent event = new ReadEvent();
        beginJfrEvent(event);
        txro.executeWithoutResult(action);
        endJfrEvent(event, e -> ((ReadEvent) e).ctxId = ctxId);
    }
}
