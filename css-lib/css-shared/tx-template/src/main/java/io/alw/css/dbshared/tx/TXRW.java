package io.alw.css.dbshared.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

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
        return txrw.execute(action);
    }

    public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
        txrw.executeWithoutResult(action);
    }
}
