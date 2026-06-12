package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeLegType;

public record Ids(TradeLegType linkType, long cashflowID, int cashflowVersion, long tradeID, int tradeVersion) {
}
