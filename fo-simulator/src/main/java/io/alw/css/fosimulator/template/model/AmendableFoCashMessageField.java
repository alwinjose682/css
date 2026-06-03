package io.alw.css.fosimulator.template.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/// NOTE: More type of amendments are possible. Ex: amendment of other fields and amendment of combination of two or more fields.
/// But, type of amendments are limited to only these certain types that are more relevant for a settlement system
public sealed interface AmendableFoCashMessageField permits AmendableFoCashMessageFieldSupplier, AmendableFoCashMessageField.Amount, AmendableFoCashMessageField.CounterpartyCode, AmendableFoCashMessageField.ValueDate {
    record ValueDate(LocalDate date) implements AmendableFoCashMessageField{public static ValueDate withZeroValue(){return new ValueDate(LocalDate.EPOCH);}}
    record Amount(BigDecimal value) implements AmendableFoCashMessageField{public static Amount withZeroValue(){return new Amount(new BigDecimal("0"));}}
    record CounterpartyCode(String code) implements AmendableFoCashMessageField{public static CounterpartyCode withZeroValue(){return new CounterpartyCode("ZERO-VAL_DO-NOT-USE");}}
}
