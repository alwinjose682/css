package io.alw.css.fosimulator.template.model;

import io.alw.css.fosimulator.template.domain.CashLegType;

public record Ids(CashLegType linkType, long cashflowID, int cashflowVersion, long tradeID, int tradeVersion) {
}
