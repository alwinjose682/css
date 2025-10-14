package io.alw.css.dbshared.tx;

import io.alw.css.profiling.ContextAwareEvent;
import jdk.jfr.*;

@Name("db.ints.txTemplateRead")
@Label("TxTemplate Read")
@Category("Database Interactions")
@StackTrace(false)
public class ReadEvent extends ContextAwareEvent {

    public ReadEvent() {
        super();
    }

    public ReadEvent(String ctxId) {
        super(ctxId);
    }
}
