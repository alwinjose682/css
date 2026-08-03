package io.alw.css.tradeconsumer.cashflow.model;

import java.math.BigDecimal;

public record GeneratorIds(
        BigDecimal tradeId,
        BigDecimal matchEventId
) {
}
