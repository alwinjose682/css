package io.alw.css.domain.common;

public enum RevisionType {
    NEW, // Initial version
    CAN, // Cashflow outright cancellation. Not an offset
    COR, // Amended version
    REV  // Offsetting reversal. RevisionType.REV is required to correctly identify offsetting reversal cashflow in all scenarios. Ex: when looking at a set of cashflows in java layer to distinguish outright cancelled cashflow and an offsetting reversal cashflow
}
