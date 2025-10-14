package io.alw.css.profiling;

import jdk.jfr.Contextual;
import jdk.jfr.Event;
import jdk.jfr.Label;

public abstract class ContextAwareEvent extends Event {

    @Contextual
    @Label("CtxId")
    public String ctxId;

    protected ContextAwareEvent() {
    }

    protected ContextAwareEvent(String ctxId) {
        this.ctxId = ctxId;
    }
}
