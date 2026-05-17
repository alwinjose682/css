package io.alw.css.domain.cashflow;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Can be any related reference depending on the link type, ex: other leg of an FX deal, CorporateAction's ref, OrderNumber
 */
@RecordBuilder
public record TradeLink(
        @NotNull
        String linkType, // Can be any value or one of the enum of type io.alw.css.domain.cashflow.TradeLinkType
        String relatedReference,
        long relatedFoCashflowID,
        int relatedFoCashflowVersion,
        long relatedTradeID,
        int relatedTradeVersion
) {
}
