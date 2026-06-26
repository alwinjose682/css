package io.alw.css.cashflowconsumer.processor;

import io.alw.css.cashflowconsumer.model.jpa.TradeLinkEntity;
import io.alw.css.cashflowconsumer.processor.rule.RevisionTypeResolver;
import io.alw.css.cashflowconsumer.util.CashflowUtil;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.cashflow.CashflowConstants;
import io.alw.css.domain.common.*;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.serialization.trade.TradeLegAvro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.alw.css.cashflowconsumer.model.constants.ExceptionSubCategoryType.*;


/// @see #mapToDomain
public final class TradeMapper {
    private final static Logger log = LoggerFactory.getLogger(TradeMapper.class);

    /// 1. Creates multiple [CashflowBuilder] for each [TradeLegAvro] and
    /// 2. Determines [Cashflow#revisionType]
    /// 3. Maps below sections of [io.alw.css.domain.trade.Trade] to CSS [Cashflow]
    /// - Trade and Cashflow Data
    /// - Obligation Data
    /// 4. Sets the precision to 10 for [Cashflow#rate]
    /// 5. Negates the amount to negative if [io.alw.css.domain.trade.TradeLeg#payOrReceive] is PAY and sets the precision to [PaymentConstants#AMOUNT_SCALE] for [Cashflow#amount]
    ///
    /// NOTE: The cashflowId and version are determined at the last step of cashflow processing, ie; after all the validations and verifications
    ///
    /// @return CashflowBuilder
    public static List<CashflowBuilder> mapToDomain(TradeAvro trd, InputBy inputBy, String inputByUserId) {
        doBasicValidationOfTradeVersion(trd);

        var bdrs = new ArrayList<CashflowBuilder>();
        for (TradeLegAvro trdLeg : trd.getTradeLegs()) {
            CashflowBuilder bdr = mapTradeLeg(trd, trdLeg, inputBy, inputByUserId);
            determineRevisionType(bdr, trd, trdLeg);
            bdrs.add(bdr);
        }

        log.debug("Mapped Trade values to [{}] Cashflows. TradeId-Ver: {}-{}", bdrs.size(), trd.getTradeID(), trd.getTradeVersion());
        return bdrs;
    }

    /// Determines the appropriate [Cashflow#revisionType] based on:
    /// - [io.alw.css.domain.trade.Trade#tradeType],
    /// - [io.alw.css.domain.trade.TradeLeg#tradeEventType],
    /// - [io.alw.css.domain.trade.TradeLeg#tradeEventAction]
    /// - [io.alw.css.domain.trade.Trade#tradeVersion] and [TradeLeg#tradeLegVersion()]
    public static void determineRevisionType(CashflowBuilder bdr, TradeAvro trd, TradeLegAvro trdLeg) {
        TradeEventType tradeEventType = mapTradeEventType(trdLeg);
        TradeEventAction tradeEventAction = mapTradeEventAction(trdLeg);
        TradeType tradeType = bdr.tradeType();
        boolean isInitialCashflowVersion = CashflowUtil.isInitialVersion(bdr.tradeVersion(), bdr.tradeLegVersion());

        log.trace("Firing RevisionType resolver rule. tradeType: {}, tradeEventType: {}, tradeEventAction: {}, isInitialCashflowVer: {}", tradeType, tradeEventType, tradeEventAction, isInitialCashflowVersion);
        RevisionType revisionType = RevisionTypeResolver.resolve(isInitialCashflowVersion, tradeType, tradeEventType, tradeEventAction);
        bdr.revisionType(revisionType);

        log.info("Computed revisionType[{}] for TradeId-Ver: {}-{}, TradeLegId-Ver: {}-{}", revisionType, trd.getTradeID(), trd.getTradeVersion(), trdLeg.getTradeLegId(), trdLeg.getTradeLegVersion());
    }

    private static CashflowBuilder mapTradeLeg(TradeAvro trd, TradeLegAvro trdLeg, InputBy inputBy, String inputByUserId) {
        var bdr = CashflowBuilder.builder()
                // Cashflow Entry Audit
                .inputDateTime(LocalDateTime.now())
                .inputBy(inputBy)
                .inputByUserID(inputByUserId)
                // Trade Data
                .tradeId(trd.getTradeID())
                .tradeVersion(trd.getTradeVersion())
                .tradeType(mapTradeType(trd))
                .transactionType(mapTransactionType(trd))
                // Trade Leg Data
                .tradeLegId(trdLeg.getTradeLegId())
                .tradeLegVersion(trdLeg.getTradeLegVersion())
                .tradeLegType(mapTradeLegType(trd, trdLeg))
                .bookCode(trdLeg.getBookCode())
                .rate(formatRate(trd, trdLeg))
                .valueDate(trdLeg.getValueDate())
                // Trade Leg - ObligationData
                .entityCode(trdLeg.getEntityCode())
                .counterpartyCode(trdLeg.getCounterpartyCode())
                .amount(formatAmount(trdLeg))
                .currCode(trdLeg.getCurrCode().toUpperCase());

        // Set counterBookCode
        String counterBookCode = bdr.transactionType() == TransactionType.INTER_BOOK ? trdLeg.getCounterBookCode() : null;
        bdr.counterBookCode(counterBookCode);

        return bdr;
    }

    public static List<TradeLinkEntity> mapTradeLinksToEntity(TradeAvro trd) {
        List<io.alw.css.serialization.trade.TradeLinkAvro> tradeLinks = trd.getTradeLinks();
        if (tradeLinks != null) {
            return tradeLinks.stream().map(tla -> {
                var tle = new TradeLinkEntity();
                tle.setTradeId(trd.getTradeID());
                tle.setTradeVersion(trd.getTradeVersion());
                tle.setLinkType(tla.getLinkType());
                tle.setRelatedReference(tla.getRelatedReference());
                tle.setRelatedTradeId(tla.getRelatedTradeId());
                tle.setRelatedTradeVersion(tla.getRelatedTradeVersion());
                return tle;
            }).toList();
        } else {
            return null;
        }
    }

//    private  LocalDate mapValueDate(TradeAvro trd, TradeLegAvro trdLeg) {
//        String valueDate = trdLeg.getValueDate();
//        try {
//            return DateUtil.formatValueDate(valueDate);
//        } catch (DateTimeParseException e) {
//            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[valueDate] is invalid", new ExceptionSubCategory(INVALID_VALUE_DATE, trd));
//        }
//    }

    private static void doBasicValidationOfTradeVersion(TradeAvro trd) {
        int tradeVersion = trd.getTradeVersion();
        if (tradeVersion < CashflowConstants.TRADE_FIRST_VERSION) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeVersion] is invalid", new ExceptionSubCategory(INVALID_FO_VERSION, trd));
        }
    }

    private static BigDecimal formatRate(TradeAvro trd, TradeLegAvro trdLeg) {
        BigDecimal rate = trdLeg.getRate();
        if (rate != null) {
            rate = rate.setScale(PaymentConstants.RATE_SCALE, RoundingMode.HALF_DOWN);
            return rate;
        } else {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[rate] is null", new ExceptionSubCategory(INVALID_RATE, trd));
        }
    }

    private static BigDecimal formatAmount(TradeLegAvro trdLeg) {
        BigDecimal amount = trdLeg.getAmount();
        if (amount != null) {
            amount = amount.setScale(PaymentConstants.AMOUNT_SCALE, RoundingMode.HALF_DOWN);
            return PayOrReceive.valueOf(trdLeg.getPayOrReceive()) == PayOrReceive.PAY
                    ? amount.negate()
                    : amount;
        } else {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[amount] is null", new ExceptionSubCategory(INVALID_AMOUNT, trdLeg));
        }
    }

    private static TransactionType mapTransactionType(TradeAvro trd) {
        if (trd.getTransactionType() == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[transactionType] is null", new ExceptionSubCategory(INVALID_MESSAGE, trd));
        }

        try {
            return TransactionType.valueOf(trd.getTransactionType());
        } catch (IllegalArgumentException e) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[transactionType] is invalid", new ExceptionSubCategory(INVALID_TRANSACTION_TYPE, trd));
        }
    }

    private static TradeType mapTradeType(TradeAvro trd) {
        var tradeType = trd.getTradeType();
        if (tradeType == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeType] is null", new ExceptionSubCategory(INVALID_MESSAGE, trd));
        }
        try {
            return TradeType.valueOf(tradeType);
        } catch (IllegalArgumentException e) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeType] is invalid", new ExceptionSubCategory(INVALID_TRADE_TYPE, trd));
        }
    }

    private static TradeLegType mapTradeLegType(TradeAvro trd, TradeLegAvro trdLeg) {
        if (trdLeg.getTradeLegType() == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeLegType] is null", new ExceptionSubCategory(INVALID_MESSAGE, trd));
        }

        try {
            return TradeLegType.valueOf(trdLeg.getTradeLegType());
        } catch (IllegalArgumentException e) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeLegType] is invalid", new ExceptionSubCategory(INVALID_TRADE_LEG_TYPE, trd));
        }
    }

    public static TradeEventType mapTradeEventType(TradeLegAvro trdLeg) {
        String tradeEventType = trdLeg.getTradeEventType();
        if (tradeEventType == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeEventType] is null", new ExceptionSubCategory(INVALID_MESSAGE, trdLeg));
        }
        try {
            return TradeEventType.valueOf(tradeEventType);
        } catch (IllegalArgumentException e) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeEventType] is invalid", new ExceptionSubCategory(INVALID_TRADE_EVENT_TYPE, trdLeg));
        }
    }

    public static TradeEventAction mapTradeEventAction(TradeLegAvro trdLeg) {
        String tradeEventAction = trdLeg.getTradeEventAction();
        if (tradeEventAction == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeEventAction] is null", new ExceptionSubCategory(INVALID_MESSAGE, trdLeg));
        }
        try {
            return TradeEventAction.valueOf(tradeEventAction);
        } catch (IllegalArgumentException e) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("fields[tradeEventAction] is invalid", new ExceptionSubCategory(INVALID_TRADE_EVENT_ACTION, trdLeg));
        }
    }
}
