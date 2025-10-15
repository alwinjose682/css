package io.alw.css.dbshared.tx;

import io.alw.css.profiling.ContextAwareEvent;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name("db.ints.txTemplateWrite")
@Label("TxTemplate Write")
@Category("Database Interactions")
@StackTrace(false)
public class WriteEvent extends ContextAwareEvent {

    public WriteEvent() {
    }

    public WriteEvent(String ctxId) {
        super(ctxId);
    }
}
