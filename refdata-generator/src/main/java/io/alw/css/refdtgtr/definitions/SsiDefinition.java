package io.alw.css.refdtgtr.definitions;

import io.alw.css.domain.cashflow.TradeType;
import io.alw.css.domain.referencedata.Counterparty;
import io.alw.css.domain.referencedata.Currency;
import io.alw.css.domain.referencedata.Ssi;
import io.alw.css.domain.referencedata.SsiBuilder;
import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.definition.BaseDefinition;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.datagen.model.AffixPosition;
import io.alw.css.refdtgtr.model.TestDataType;
import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.AlphaNumericTokenGenerator;
import io.alw.datagen.tokengenerator.LongTokenGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SsiDefinition extends BaseDefinition<Ssi> {
    private final SsiDefinitionUtilities utilities;
    private final SsiBuilder ssiBuilder;
    private final Counterparty counterparty;
    private final Currency currency;
    private final TradeType product;

    public SsiDefinition(Counterparty counterparty, Currency currency, TradeType product) {
        this(null, counterparty, currency, product);
    }

    private SsiDefinition(Ssi parent, Counterparty counterparty, Currency currency, TradeType product) {
        super(parent);
        this.utilities = SsiDefinitionUtilities.singleton();
        this.ssiBuilder = SsiBuilder.builder();
        this.counterparty = counterparty;
        this.currency = currency;
        this.product = product;
    }

    @Override
    public SsiDefinition withDefaults() {
        AlphaNumericTokenGenerator idGenerator = utilities.idGenerator;
        StringRefDataProvider stringRefDataProvider = utilities.stringRefDataProvider;
        LongTokenGenerator bnkAccNumGntr = utilities.bankAccountNumberGenerator;
        TokenFormattingTemplate<String, String> bicCodeTemplate = utilities.bicCodeTemplate;
        TokenFormattingTemplate<String, String> bnkAccNumTmplt = utilities.bankAccountNumberTemplate;

        ssiBuilder
                .ssiID(idGenerator.nextAsString())
                .ssiVersion(ConfigParams.FIRST_TEST_DATA_VERSION)
                .counterpartyCode(counterparty.counterpartyCode())
                .counterpartyVersion(counterparty.counterpartyVersion())
                .currCode(currency.currCode())
                .product(product)
                .primary(isParentDefinition())
                .beneType(isParentDefinition() ? stringRefDataProvider.next(TestDataType.COUNTERPARTY_TYPE) : parent.beneType())
                .bankBic(bicCodeTemplate.apply("", getBicCodeTokenValues("SB"), "X", AffixPosition.SUFFIX, 1).toUpperCase())
                .bankAccount(bnkAccNumTmplt.apply("0", List.of(bnkAccNumGntr.nextAsString()), "0"))
                .bankLine1(utilities.stringRefDataProvider.next(TestDataType.STATE_OR_CITY_OR_STREET_GENERIC_NAMES))
                .corrBic(utilities.isAnNthDefinition(10) ? bicCodeTemplate.apply("", getBicCodeTokenValues("CR"), "X", AffixPosition.SUFFIX, 2).toUpperCase() : null)
                .corrAccount(utilities.isAnNthDefinition(10) ? bnkAccNumTmplt.apply("", List.of(bnkAccNumGntr.nextAsString()), "00") : null)
                .corrLine1(utilities.isAnNthDefinition(10) ? utilities.stringRefDataProvider.next(TestDataType.STATE_OR_CITY_OR_STREET_GENERIC_NAMES) : null)
                .active(true)
                .entryTime(LocalDateTime.now())
        ;

        return this;
    }

    private List<String> getBicCodeTokenValues(String thirdTokenValue) {
        String currentId = utilities.idGenerator.currentAsString();
        // entity's state is taken just to form a random 2 char String. The nostro's bankBic and corrBic fields(not beneBic) have no relation to the entity
        String state = utilities.stringRefDataProvider.next(TestDataType.STATE_OR_CITY_OR_STREET_GENERIC_NAMES);
        ArrayList<String> bicCodeTokenValues = new ArrayList<>();
        bicCodeTokenValues.add(currentId);
        bicCodeTokenValues.add((state.charAt(0) + "" + state.charAt(state.length() - 1)).toUpperCase());
        bicCodeTokenValues.add(thirdTokenValue);
        return Collections.unmodifiableList(bicCodeTokenValues);
    }

    @Override
    public Ssi buildDefinition() {
        return ssiBuilder.build();
    }

    @Override
    protected SsiDefinition childDefinition(Ssi parent) {
        return new SsiDefinition(parent, this.counterparty, this.currency, this.product).withDefaults();
    }
}
