package io.alw.css.fosimulator.template.common;

import io.alw.css.fosimulator.model.CashLegType;

public record Ids(CashLegType linkType, long cashflowID, int cashflowVersion, long tradeID, int tradeVersion) {
}
