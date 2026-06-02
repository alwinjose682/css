package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.template.model.CashLegType;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.model.Ids;
import io.alw.css.fosimulator.template.model.MessageContext;
import io.alw.datagen.provider.AbstractCyclicDataProvider;
import io.alw.datagen.template.AggregateTemplateBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/// This class is not concurrent safe / thread safe.
///
/// [CashMessageTemplate] instances are both a trade type template and a supplier of the build output of the template
/// Each instance of this class is supposed to be exclusive for a single thread
sealed abstract class CashMessageTemplate<M extends MessageContext>
        extends AggregateTemplateBuilder<M, FoCashMessageBuilder, FoCashMessage>
        implements Supplier<List<FoCashMessage>>
        permits CashMessageTemplateWithDataStore {

    /// Variable values for each template build. These values remain un-modified for each template build.
    /// After each build of the template, the [MessageContext] (msgCtx) and [FoCashMessageBuilder] (`bdr`) references are just assigned with new instances
    // Message Context and FoCashMessage builder
    private M msgCtx;
    private FoCashMessageBuilder bdr;

    // Constant values for each instance of CashMessageTemplate
    private final String entityCode;
    private final String currCode;
    private final TradeType tradeType;
    private final TransactionType transactionType;
    protected final RandomGenerator rndm;
    protected final CashMessageTemplateHelper msgTemplateHelper;

    // Spring Beans
    protected final DayTicker dayTicker;
    protected final RefDataService refDataService;

    //Common Constants
    protected final static int VERSION_ONE = 1;
    protected final static Supplier<BigDecimal> cyclicRateProvider = new CyclicRateProvider(getRateList());

    public CashMessageTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        this(null, entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    private CashMessageTemplate(M parent, Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
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

    /// Build the grouped or related cash message associated with the cashMessage template being built
    /// tradeLinks of grouped and related items are set when creating a cashMessage builder via [CashMessageTemplate#createBuilderFrom(FoCashMessage, String)]
    /// The resultant [FoCashMessage] is already associated with the [MessageContext] in prior steps(using callbacks when creating new trade, amendments and new grouped cashMessages)
    @Override
    protected FoCashMessage buildGroupedOrRelatedItem(FoCashMessageBuilder bdr) {
        return bdr.build();
    }

    /// Builds the cashMessage template
    ///
    /// **tradeLinks of root FoCashMessage**:
    /// tradeLinks of rootFoCashMessage are set when creating a cashMessage builder with the default values via the method: [CashMessageTemplate#getNewCashMsgBuilder(Ids, MessageContext)]
    @Override
    public M buildRootTemplate() {
        msgCtx.setRootFoCashMessage(bdr.build());
        return msgCtx;
    }

    /// This method ensures that the same day is used at all points of building the template.
    /// This method is the starting point to build a template
    protected CashMessageTemplate<M> newTemplateBuilder() {
        msgTemplateHelper.setDayForMsgTemplate(dayTicker.day());
        return this;
    }

    /// This method is used to create root [FoCashMessageBuilder]. The builder for grouped or related items are created using [CashMessageTemplate#createBuilderFrom(FoCashMessage, String)]
    /// NOTE: New [CashMessageTemplate] instances are not created by this method.
    /// Instead, the existing [FoCashMessageBuilder] (`bdr`) is just replaced with a new one and then new values are assigned.
    protected FoCashMessageBuilder getNewCashMsgBuilder(Ids rooCashMessageIds, M msgCtx) {
        msgTemplateHelper.incrementCounter();
        this.bdr = FoCashMessageBuilder.builder();
        this.msgCtx = msgCtx;

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
                .tradeID(rooCashMessageIds.tradeID())
                .tradeVersion(rooCashMessageIds.tradeVersion())
                .cashflowID(rooCashMessageIds.cashflowID())
                .cashflowVersion(rooCashMessageIds.cashflowVersion())
                // TradeLink for root cash message(This method is invoked only for root cash message)
                .tradeLinks(List.of(CashMessageTemplateHelper.mapToTradeLink(rooCashMessageIds)))
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

    /// This method is used to create [FoCashMessageBuilder] for grouped or related cashMessages. The root [FoCashMessageBuilder] is created using [CashMessageTemplate#getNewCashMsgBuilder(Ids, MessageContext)]
    /// TradeLink is also set in this method using the linkType parameter. Further build steps can add more tradeLinks to the list if needed
    ///
    /// NOTE: The [CashMessageTemplate#counter] is not incremented by this method
    protected FoCashMessageBuilder createBuilderFrom(FoCashMessage cashMsg, CashLegType linkType) {
        FoCashMessage rootFoCashMessage = msgCtx.rootFoCashMessage();
        TradeLink tradeLink = TradeLinkBuilder.TradeLink(
                linkType.name, null,
                rootFoCashMessage.cashflowID(), rootFoCashMessage.cashflowVersion(),
                rootFoCashMessage.tradeID(), rootFoCashMessage.tradeVersion());

        var tradeLinkList = new ArrayList<TradeLink>();
        tradeLinkList.add(tradeLink);

        return FoCashMessageBuilder
                .builder(cashMsg)
                .tradeLinks(tradeLinkList);
    }

    private static List<BigDecimal> getRateList() {
        var rateStr = List.of(
                "2.1185083", "2.8868305", "1.8473637", "2.2454485", "3.3116012", "2.1255246", "3.1924071", "2.2949433", "1.6131843", "3.1691975", "2.0812858", "3.4524542", "2.9438653", "2.443113", "1.3269241", "2.0300792", "2.5547008", "1.5003698", "1.6737637", "2.8289966", "3.4471569", "3.6187675", "2.9016997", "2.8803334", "1.3131017", "1.626014", "2.6651969", "2.6402754", "2.6658711", "3.2721593", "2.8354132", "1.7805847", "2.5856666", "1.7802754", "3.5435262", "2.7626117", "3.1518534", "2.5784921", "3.3303213", "1.6806611", "1.8080688", "1.5369123", "2.1740543", "3.280933", "2.5091885", "1.5163557", "2.4127834", "2.9464712", "3.3048503", "1.3637208", "2.1320399", "2.1680598", "3.182202", "1.8264055", "2.3940446", "1.5310284", "1.865899", "2.1588027", "1.5499161", "1.2397343", "3.4788844", "2.7990731", "2.4957322", "1.6263971", "3.5495777", "1.929513", "1.6045214", "2.3645215", "2.3601396", "2.4321751", "2.5018222", "2.636745", "3.0383471", "2.7894212", "1.4200827", "2.9931458", "3.4883197", "3.2753174", "3.1795485", "3.0840345", "1.9228909", "1.4031125", "2.1217102", "2.8524539", "1.3567054", "2.4015993", "3.5593782", "3.3028495", "2.687543", "2.8682274", "1.8368547", "2.3705287", "3.0995824", "2.9572113", "2.7740095", "2.5384895", "2.0613349", "2.072932", "1.9655553", "1.6273281", "3.6478887", "3.6300205", "1.6626679", "3.7237054", "3.610583", "3.237749", "3.6605441", "2.5049929", "3.6498713", "3.7336006", "3.4246226", "1.2173991", "1.6758315", "3.0168944", "3.323559", "2.6024157", "3.6187579", "3.3423974", "2.7813524", "3.0106167", "2.8212653", "1.9305919", "2.606853", "1.9300306", "1.242904", "1.885036", "3.6149268", "3.3132719", "3.2483148", "3.0108361", "2.8211316", "3.3029408", "2.1273866", "1.7025346", "3.3848867", "1.4498513", "1.2602615", "3.6573901", "2.527801", "1.6929437", "1.3919333", "2.2545699", "2.5504785", "3.1661702", "3.5523055", "3.2025034", "2.0930714", "3.2643422", "1.7244502", "3.4753173", "2.6790988", "1.4950725", "3.5794776", "3.2595445", "3.4596", "1.7122343", "1.2604124", "3.6968327", "1.6019635", "1.9457021", "2.8826098", "3.0017222", "1.8418536", "2.1665186", "2.2869395", "2.589536", "1.3557462", "3.3454516", "3.0423961", "2.7504139", "1.323162", "3.6255396", "3.1489708", "2.9907934", "2.7305057", "2.7492652", "3.5530467", "2.0283751", "2.4740956", "1.8600393", "3.0638062", "2.0541499", "2.7736989", "2.0421414", "2.5708573", "1.5843675", "2.4622258", "1.4963566", "2.8312581", "1.3092927", "2.5206927", "2.1408122", "3.4650055", "1.5857169", "2.8564981", "3.2527169", "3.4259838", "3.5813454", "3.4732048", "3.6239507", "2.3331112", "2.2050705", "3.6320499", "1.2590111", "1.3404848", "3.1940757", "3.4508125", "2.4746294", "2.7260973", "2.9638277", "2.5304446", "1.5718931", "1.8659552", "2.7516587", "1.7639439", "3.018325", "3.3819471", "2.4339096", "3.6011392", "1.6779693", "3.2504233", "2.1549363", "1.6203486", "1.4666654", "1.9949926", "2.9104596", "2.291073", "2.8075635", "2.8487312", "3.4502674", "2.7800702", "1.2625622", "3.5611977", "1.5380502", "2.69774", "2.6620734", "3.5399706", "3.0452954", "2.2656463", "3.5604138", "2.8323526", "2.2070762", "2.5347264", "1.4622472", "2.6621177", "3.4306836", "2.1771628", "3.4380949", "2.0234889", "2.7519604", "3.4396385", "3.2566278", "1.8508986", "1.4512332", "1.5355585", "2.7940728", "3.2923905", "2.6816069", "1.8816903", "3.0353643", "3.1561233", "1.2697359", "2.5678602", "3.6098247", "2.8326218", "1.9003624", "2.0748241", "2.9074877", "2.9860683", "3.5599286", "2.8499946", "3.2552502", "2.7274038", "1.8825112", "1.2386079", "3.2068213", "1.9471561", "3.5282402", "1.3590564", "2.4383917", "1.4334206", "2.4864557", "2.8812244", "2.1525237", "3.1585163", "3.3205197", "3.5922941", "1.5530544", "2.673579", "3.4414859", "1.5535759", "2.6449131", "3.6226382", "3.3784338", "2.2642544", "3.4060248", "2.4785952", "3.3322703", "2.2672309", "1.8474128", "3.3912351", "1.2668012", "3.7389125", "2.7140252", "2.2277313", "1.8923821", "1.5744029", "1.89105", "2.6339162", "3.5631594", "3.7449905", "3.4469785", "1.7916661", "2.0576988", "2.6040234", "2.4806892", "3.5311801", "2.0889187", "3.514689", "2.0977981", "3.2750386", "2.6192852", "1.3051302", "1.6709274", "1.8660551", "1.2921503", "1.9794271", "2.9932475", "2.2233883", "3.6191502", "1.7790197", "3.4733397", "1.304437", "1.4636379", "1.3969901", "1.3353683", "2.4080642", "1.4357657", "1.3509265", "2.2024706", "2.1432288", "2.4299856", "1.2539267", "1.8235075", "1.6751541", "2.8961799", "3.1671056", "1.5082203", "3.2588643", "1.812882", "3.4830889", "2.1409468", "1.4351873", "3.4835164", "2.335755", "3.3412899", "2.9094089", "1.8156153", "3.0827859", "3.0298379", "3.0992536", "2.8481176", "1.7513404", "2.7493984", "1.7476302", "2.1214895", "1.6353642", "2.2883555", "2.5939944", "3.4474431", "1.4864974", "1.2908499", "1.8323544", "1.7082011", "2.3758736", "1.8826395", "2.0136666", "2.2841307", "1.6993843", "1.4980542", "3.6451258", "1.9203278", "2.7932988", "3.3658713", "3.1708044", "2.3392663", "3.6668955", "1.5569553", "2.9292153", "3.427403", "3.5675187", "1.7959063", "2.2892614", "1.6656289", "3.3531489", "3.0705163", "1.8556407", "2.0185589", "3.3814664", "1.3849084", "1.2835652", "1.3110915", "3.1855292", "3.3533653", "3.3058329", "1.3978592", "2.3711291", "2.5812491", "2.6882699", "3.3423992", "2.7257732", "1.5723154", "1.6668146", "2.9259791", "3.5515031", "1.3213131", "1.4449212", "3.515521", "1.4538591", "3.22496", "2.1957498", "2.3626904", "1.7753338", "3.3431637", "3.6371913", "1.9353149", "2.2365581", "3.3458661", "2.7232919", "1.2208947", "1.5757898", "1.5356657", "1.8629967", "2.7619501", "3.3332856", "2.6424623", "3.6634907", "2.8383757", "2.4990055", "1.6651246", "3.6762634", "3.1850314", "1.6084304", "3.2975658", "3.7425108", "1.63977", "3.22307", "1.3949212", "2.3096374", "2.0145763", "1.5751076", "3.4097807", "1.9456207", "3.5636626", "3.4884709", "2.4036356", "2.5431385", "2.7444305", "1.4775711", "1.8366066", "2.3712474", "2.636049", "1.3005977", "3.5262989", "2.3520486", "1.9377509", "2.321033", "3.0619267", "1.7621371", "2.3284856", "1.8707817", "2.5729036", "2.3574131", "1.6593076", "3.6063992", "2.2769396", "1.2820094", "1.9493569", "2.2686373", "3.3568833", "2.735097", "1.5174597", "3.215152", "2.2737137", "1.4368884", "2.1428744", "3.0935377", "2.3936668", "1.3024853", "3.4578443", "2.2358582", "3.5242928", "2.5792977", "1.8721812", "3.503071", "1.7816667", "2.2163525", "2.9229441", "3.6747243", "3.5384014"
        );
        return rateStr.stream().map(rs -> new BigDecimal(rs).setScale(7, RoundingMode.HALF_UP)).toList();
    }

    private static class CyclicRateProvider extends AbstractCyclicDataProvider<BigDecimal> {
        protected CyclicRateProvider(List<BigDecimal> dataList) {
            super(dataList);
        }
    }
}
