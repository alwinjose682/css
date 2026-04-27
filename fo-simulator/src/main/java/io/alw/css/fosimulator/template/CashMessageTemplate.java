package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.datagen.template.TemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/// This class is not concurrent safe / thread safe.
///
/// [CashMessageTemplate] instances are both a trade type template and a supplier of the build output of the template
/// Each instance of this class is supposed to be exclusive for a single thread
sealed abstract class CashMessageTemplate
        extends TemplateBuilder<FoCashMessage>
        implements Supplier<List<FoCashMessage>>
        permits CashMessageTemplateWithDataStore {

    // Variable values for each template build. Also, these remain un-modified for each template build.
    /// After each build of the template, the existing [FoCashMessageBuilder] (`bdr`) is just replaced with a new one.
    private FoCashMessageBuilder bdr;

    // Fixed values for each instance of CashMessageTemplate
    private final String entityCode;
    private final String currCode;
    private final TradeType tradeType;
    private final TransactionType transactionType;
    protected final RandomGenerator rndm;
    protected final CashMessageTemplateHelper msgTemplateHelper;

    // Spring Beans
    protected final DayTicker dayTicker;
    protected final RefDataService refDataService;

    public CashMessageTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        this(null, entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    private CashMessageTemplate(FoCashMessage parent, Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(parent);
        this.entityCode = entity.entityCode();
        this.currCode = entity.currCode();
        this.tradeType = tradeType;
        this.transactionType = transactionType;
        this.rndm = rndm;
        this.msgTemplateHelper = new CashMessageTemplateHelper(initialValueDate, transactionType, rndm, cashMsgTemplateProps, refDataService);
        this.dayTicker = dayTicker;
        this.refDataService = refDataService;
    }

    protected abstract TradeEventActionPair getNextEventActionPair(TradeEventType amendMsgEvt, TradeEventAction amendMsgAct);

    /// This method ensures that the same day is used at all points of building the template.
    /// This method is the starting point to build a template
    protected CashMessageTemplate newTemplateBuilder() {
        msgTemplateHelper.setDayForMsgTemplate(dayTicker.day());
        return this;
    }

    /// NOTE: New [CashMessageTemplate] instances are not created by this method.
    /// Instead, the existing [FoCashMessageBuilder] (`bdr`) is just replaced with a new one and then new values are assigned.
    protected FoCashMessageBuilder getFoCashMsgBuilderForNewTemplate() {
        msgTemplateHelper.incrementCounter();
        bdr = FoCashMessageBuilder.builder();

        IdProvider idProvider = IdProvider.singleton();
        final String counterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionType();
        bdr
                // Fixed value for this template
                .entityCode(this.entityCode)
                .currCode(this.currCode)
                .tradeType(tradeType)
                .transactionType(transactionType)
                // Always a new trade
                .tradeEventType(TradeEventType.NEW_TRADE)
                .tradeEventAction(TradeEventAction.ADD)
                // Id values
                .tradeID(idProvider.nextTradeId())
                .tradeVersion(1)
                .cashflowID(idProvider.nextCashflowId())
                .cashflowVersion(1)
                // Entity dependent fields. Book codes are dummy for now
                .bookCode(refDataService.dummyBookCode())
                .counterBookCode(msgTemplateHelper.isInterbookTransaction() ? refDataService.dummyCounterBookCode() : null) // Also a TransactionType dependent
                // TransactionType dependent fields
                .counterpartyCode(counterpartyCode)
                // Others
                .rate(new BigDecimal("1.2154754")) // rate is just a constant. No rate dependent calculation is done in CSS
        ;

        return bdr;
    }

    /// NOTE: The [CashMessageTemplate#counter] is not incremented by this method
    protected FoCashMessageBuilder createBuilderFrom(FoCashMessage cashMsg) {
        return FoCashMessageBuilder.builder(cashMsg);
    }

    @Override
    public FoCashMessage buildTemplate() {
        return bdr.build();
    }

    @Override
    protected TemplateBuilder<FoCashMessage> childTemplate(FoCashMessage parent) {
        throw new RuntimeException("This method is not supported for CashMessageTemplate");
    }
}
