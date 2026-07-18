package io.alw.css.confirmation;

public sealed interface LongId permits ConfirmationMatchRequest, ConfirmationMatchEvent {
    long id();
}