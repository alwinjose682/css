package io.alw.css.fosimulator.template.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/// NOTE: Amendments are limited to only these types and their combinations. Amendments of other fields like bookCode etc are not implemented
///
/// NOTE: [AmendableFieldSupplier] extends [AmendableField]
public sealed interface AmendableField permits AmendableFieldSupplier, AmendableField.Amount, AmendableField.CounterpartyCode, AmendableField.ValueDate {
    record ValueDate(LocalDate date) implements AmendableField {public static AmendmentTarget amendmentTarget = AmendmentTarget.TRADE_LEG;}
    record Amount(BigDecimal value) implements AmendableField {public static AmendmentTarget amendmentTarget = AmendmentTarget.TRADE_LEG;}
    record CounterpartyCode(String code) implements AmendableField {public static AmendmentTarget amendmentTarget = AmendmentTarget.TRADE;}
}
