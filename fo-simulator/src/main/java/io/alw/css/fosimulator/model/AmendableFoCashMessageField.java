package io.alw.css.fosimulator.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/// NOTE: More type of amendments are possible. Ex: amendment of other fields and amendment of combination of two or more fields.
/// But, type of amendments are limited to only these certain types that are more relevant for a settlement system
public sealed interface AmendableFoCashMessageField {
    record ValueDate(LocalDate date) implements AmendableFoCashMessageField{}
    record Amount(BigDecimal value) implements AmendableFoCashMessageField{}
    record CounterpartyCode(String code) implements AmendableFoCashMessageField{}
}
