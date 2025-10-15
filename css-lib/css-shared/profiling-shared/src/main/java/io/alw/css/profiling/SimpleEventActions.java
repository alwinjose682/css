package io.alw.css.profiling;

import jdk.jfr.Event;

import java.util.function.Consumer;

/// The javadoc for [jdk.jfr.Event] says the following:
/// "
/// Gathering data to store in an event can be expensive. The shouldCommit() method can be used to verify whether an event instance would actually be written to the system when the commit() method is invoked.
/// "
/// This is the main reason to use this class.
///
/// NOTE: This class is not extendable and contains only static methods
public final class SimpleEventActions {

    public static void endJfrEvent(Event event) {
        if (notEnabled(event)) {
            return;
        }
        event.end();
        event.commit(); // 'event::commit' internally checks for 'event::shouldCommit'. So, no need to explicitly check the same
    }

    /// `action` is the step to be performed on the JFR event if the event needs to be committed.
    /// Example: `action` can be used to set the values of the event
    public static void endJfrEvent(Event event, Consumer<Event> action) {
        if (notEnabled(event)) {
            return;
        }
        event.end();

        if (!event.shouldCommit()) {
            return;
        }
        action.accept(event); //Performs the action only if the event should be committed
        event.commit();
    }

    public static void beginJfrEvent(Event event) {
        if (notEnabled(event)) {
            return;
        }

        event.begin();
    }

    /// `action` is the step to be performed on the JFR event if the event is enabled.
    public static void beginJfrEvent(Event event, Consumer<Event> action) {
        if (notEnabled(event)) {
            return;
        }

        action.accept(event); //Performs the action only if the event is enabled
        event.begin();
    }

    private static boolean notEnabled(final Event event) {
        return event == null || !event.isEnabled();
    }
}
