package io.alw.css.fosimulator.template.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/// NOTE: Amendments are limited to only these types and their combinations. Amendments of other fields like bookCode etc are not implemented
///
/// NOTE: [AmendableFoCashMessageFieldSupplier] extends [AmendableFoCashMessageField]
public sealed interface AmendableFoCashMessageField permits AmendableFoCashMessageFieldSupplier, AmendableFoCashMessageField.Amount, AmendableFoCashMessageField.CounterpartyCode, AmendableFoCashMessageField.ValueDate {
    record ValueDate(LocalDate date) implements AmendableFoCashMessageField{public static ValueDate withZeroValue(){return new ValueDate(LocalDate.EPOCH);}}
    record Amount(BigDecimal value) implements AmendableFoCashMessageField{public static Amount withZeroValue(){return new Amount(new BigDecimal("0"));}}
    record CounterpartyCode(String code) implements AmendableFoCashMessageField{public static CounterpartyCode withZeroValue(){return new CounterpartyCode("ZERO-VAL_DO-NOT-USE");}}
}
