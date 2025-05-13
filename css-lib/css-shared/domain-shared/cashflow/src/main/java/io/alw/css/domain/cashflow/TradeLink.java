package io.alw.css.domain.cashflow;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Can be any related reference depending on the link type, ex: other leg of an FX deal, CorporateAction's ref, OrderNumber
 */
public record TradeLink(
        @NotNull
        @JsonProperty("type")
        String linkType, // Can be any value or one of the enum of type io.alw.css.domain.cashflow.TradeLinkType
        @NotNull
        String relatedReference
) {
}
