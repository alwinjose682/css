package io.alw.css.domain.common;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;

/**
 * Can be any related reference depending on the link type, ex: other leg of an FX deal, CorporateAction's ref, OrderNumber
 */
@RecordBuilder
public record TradeLink(
        @NotNull
        String linkType, // Can be any value or one of the enum of type io.alw.css.domain.common.TradeLinkType
        String relatedReference,
        long relatedTradeId,
        int relatedTradeVersion
) {
}
