package io.alw.css.tradeconsumer.cashflow.service;

import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.PaymentSuppressionCategory;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.tradeconsumer.cashflow.model.NostroDetails;
import io.alw.css.tradeconsumer.cashflow.model.SsiWithCounterpartyData;
import io.alw.css.tradeconsumer.cashflow.model.properties.SuppressionConfig;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import io.alw.css.tradeconsumer.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

import static io.alw.css.domain.common.TransactionType.INTER_BOOK;

/// Enriches Cashflow
///
/// @see
public class CashflowEnrichmentService {
    private final static Logger log = LoggerFactory.getLogger(CashflowEnrichmentService.class);
    private final SuppressionConfig suppressionConfig;
    private final CacheService cacheService;

    public CashflowEnrichmentService(SuppressionConfig suppressionConfig, CacheService cacheService) {
        this.suppressionConfig = suppressionConfig;
        this.cacheService = cacheService;
    }

    public void validateAndEnrich(CashflowBuilder builder) {
        String currCode = builder.currCode();
        String counterpartyCode = builder.counterpartyCode();
        TradeType tradeType = builder.tradeType();
        String entityCode = builder.entityCode();

        SsiWithCounterpartyData ssiWithCpData = cacheService.getPrimarySsiWithCounterpartyData(counterpartyCode, currCode, tradeType);
        NostroDetails nostroDetails = cacheService.getNostroDetails(entityCode, currCode, counterpartyCode);

        validateEntityAndCurrCode(builder);
        enrichWithSsiID(builder, ssiWithCpData);
        enrichWithNostroID(builder, nostroDetails);
        setInternalValue(builder, ssiWithCpData);
        setPaymentSuppressionValue(builder);

        log.debug("Successfully Validated and Enriched the cashflow. TradeLegId-Ver: {}-{}", builder.tradeLegId(), builder.tradeLegVersion());
    }

    void validateEntityAndCurrCode(CashflowBuilder builder) {
        String entityCode = builder.entityCode();
        String currCode = builder.currCode();
        boolean entityActive = cacheService.isEntityActive(entityCode);
        boolean currencyActive = cacheService.isCurrencyActive(currCode);
        if (!entityActive) {
            throw CategorizedRuntimeException.BUSINESS_RECOVERABLE("Entity is inactive", new ExceptionSubCategory(ExceptionSubCategoryType.INACTIVE_ENTITY, null));
        } else if (!currencyActive) {
            throw CategorizedRuntimeException.BUSINESS_RECOVERABLE("Currency is inactive", new ExceptionSubCategory(ExceptionSubCategoryType.INACTIVE_CURRENCY, null));
        }
    }

    void enrichWithNostroID(CashflowBuilder builder, NostroDetails nostroDetails) {
        String entityCode = builder.entityCode();
        String currCode = builder.currCode();

        if (nostroDetails != null) {
            var primaryNostro = nostroDetails.primaryNostro();
            var overridableNostro = nostroDetails.overridableNostro();
            if (overridableNostro != null) {
                String nostroID = overridableNostro.nostroID();
                builder.nostroId(nostroID);
                log.info("Enriched NostroID[{}] by overriding primary nostro with secondary configured in counterparty profile. CounterpartyCode: {}, CurrCode: {}, EntityCode: {}, TradeLegId-Ver: {}-{}", nostroID, overridableNostro.counterpartyCode(), overridableNostro.currCode(), overridableNostro.entityCode(), builder.tradeLegId(), builder.tradeLegVersion());
                return;
            } else if (primaryNostro != null) {
                String nostroID = primaryNostro.nostroID();
                builder.nostroId(nostroID);
                log.debug("Enriched with nostroId: {}. TradeLegId-Ver: {}-{}", nostroID, builder.tradeLegId(), builder.tradeLegVersion());
                return;
            }
        }

        var msg = "Nostro is inactive or does not exist. EntityCode: " + entityCode + ", CurrCode: " + currCode;
        throw CategorizedRuntimeException.BUSINESS_RECOVERABLE(msg, new ExceptionSubCategory(ExceptionSubCategoryType.INACTIVE_OR_MISSING_NOSTRO, null));
    }

    void enrichWithSsiID(CashflowBuilder builder, SsiWithCounterpartyData ssiWithCpData) {
        String counterpartyCode = builder.counterpartyCode();
        String currCode = builder.currCode();
        TradeType tradeType = builder.tradeType();

        if (ssiWithCpData == null) {
            var msg = "Primary SSI or Counterparty is inactive or does not exist. CounterpartyCode: " + counterpartyCode + ", CurrCode: " + currCode + ", TradeType: " + tradeType;
            throw CategorizedRuntimeException.BUSINESS_RECOVERABLE(msg, new ExceptionSubCategory(ExceptionSubCategoryType.INACTIVE_OR_MISSING_SSI, null));
        } else {
            String ssiID = ssiWithCpData.ssiId();
            builder.ssiId(ssiID);
            log.debug("Enriched with ssiId: {}. TradeLegId-Ver: {}-{}", ssiID, builder.tradeLegId(), builder.tradeLegVersion());
        }
    }

    /// Suppresses the cashflow if the abs(amount) is less than or equal to the amount in suppression configuration
    void setPaymentSuppressionValue(CashflowBuilder builder) {
        String cfCurr = builder.currCode();
        BigDecimal absoluteCfAmt = builder.amount().abs();

        if (suppressionConfig.suppressInterbookTX() && builder.transactionType() == INTER_BOOK) {
            builder.paymentSuppressionCategory(PaymentSuppressionCategory.INTERBOOK);
            log.info("Cashflow will be suppressed. SuppressionCategory: {}, TradeLegId-Ver: {}-{}", PaymentSuppressionCategory.INTERBOOK, builder.tradeLegId(), builder.tradeLegVersion());
            return;
        } else if (absoluteCfAmt.compareTo(suppressionConfig.highestSuppressibleAmount()) <= 0) {
            for (Map.Entry<String, BigDecimal> entry : suppressionConfig.suppressibleCurrToAmountMap().entrySet()) {
                String curr = entry.getKey();
                BigDecimal amt = entry.getValue();
                if (curr.equalsIgnoreCase(cfCurr) && absoluteCfAmt.compareTo(amt) <= 0) {
                    builder.paymentSuppressionCategory(PaymentSuppressionCategory.AMOUNT_TOO_SMALL);
                    log.info("Cashflow will be suppressed. SuppressionCategory: {}, TradeLegId-Ver: {}-{}", PaymentSuppressionCategory.AMOUNT_TOO_SMALL, builder.tradeLegId(), builder.tradeLegVersion());
                    return;
                }
            }
        }

        builder.paymentSuppressionCategory(PaymentSuppressionCategory.NONE);
        log.trace("Cashflow is NOT suppressible. TradeLegId-Ver: {}-{}", builder.tradeLegId(), builder.tradeLegVersion());
    }

    void setInternalValue(CashflowBuilder builder, SsiWithCounterpartyData ssiWithCpData) {
        boolean internalTransactionType = switch (builder.transactionType()) {
            case INTER_BOOK, INTER_BRANCH, INTER_COMPANY -> true;
            case CLIENT, MARKET, CORPORATE_ACTION -> false;
        };

        boolean internalCounterparty = ssiWithCpData.internal();
        builder.internal(internalTransactionType && internalCounterparty);
        log.debug("Set internal/external value for the cashflow. TradeLegId-Ver: {}-{}", builder.tradeLegId(), builder.tradeLegVersion());
    }
}
