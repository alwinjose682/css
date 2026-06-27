package io.alw.css.dbshared.tx;

import io.alw.css.profiling.ContextAwareEvent;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

import static io.alw.css.profiling.SimpleEventActions.beginJfrEvent;

@Name("db.ints.txTemplateRead")
@Label("TxTemplate Read")
@Category("Database Interactions")
@StackTrace(false)
public class ReadEvent extends ContextAwareEvent {

    public ReadEvent() {
        super();
    }

    ///  Just a helper method
    public static ContextAwareEvent start() {
        var event = new ReadEvent();
        beginJfrEvent(event);
        return event;
    }
}
