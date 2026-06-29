package io.alw.css.tradeconsumer.model;

import io.alw.css.domain.common.TradeType;

public record SsiWithCounterpartyData(
        String counterpartyCode,
        int counterpartyVersion,
        boolean internal,
        boolean activeCounterparty,
        String ssiId,
        int ssiVersion,
        String currCode,
        TradeType product,
        boolean primarySsi,
        boolean activeSsi
) {
}
