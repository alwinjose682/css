package io.alw.css.domain.common;

public enum CashflowConfirmationStatus {
    PENDING, // Happens due to one of the following: 1) confirmation request NOT send, 2) confirmation request sent and awaiting match status, 3) previously confirmed cashflow is un-confirmed
    CONFIRMED // When cashflow is confirmed
}
