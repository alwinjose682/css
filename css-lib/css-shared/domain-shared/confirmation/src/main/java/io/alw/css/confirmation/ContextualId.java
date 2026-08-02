package io.alw.css.confirmation;

public sealed interface ContextualId permits ConfirmationMatchRequest, ConfirmationMatchEvent {
    long contextualId();
}