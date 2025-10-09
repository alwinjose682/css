package io.alw.css.refdtgtr.definitions;

import io.alw.css.domain.referencedata.Currency;
import io.alw.css.domain.referencedata.Entity;
import io.alw.css.domain.referencedata.Nostro;
import io.alw.css.domain.referencedata.NostroBuilder;
import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.definition.BaseDefinition;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.datagen.model.AffixPosition;
import io.alw.css.refdtgtr.model.TestDataType;
import io.alw.css.refdtgtr.provider.RefDataCollection;
import io.alw.datagen.tokengenerator.AlphaNumericTokenGenerator;
import io.alw.datagen.tokengenerator.LongTokenGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NostroDefinition extends BaseDefinition<Nostro> {
    private final NostroDefinitionUtilities utilities;
    private final NostroBuilder nsBdr;
    private final Entity entity;
    private final Currency currency;

    public NostroDefinition(Entity entity, Currency currency) {
        this(entity, currency, null);
    }

    private NostroDefinition(Entity entity, Currency currency, Nostro parent) {
        super(parent);
        this.utilities = NostroDefinitionUtilities.singleton();
        this.nsBdr = NostroBuilder.builder();
        this.entity = entity;
        this.currency = currency;
    }

    @Override
    public NostroDefinition withDefaults() {
        AlphaNumericTokenGenerator idProvider = utilities.idGenerator;
        AlphaNumericTokenGenerator slaCodeProvider = utilities.slaCodeGenerator;
        LongTokenGenerator bnkAccNumGntr = utilities.bankAccountNumberGenerator;
        TokenFormattingTemplate<String, String> bicCodeTemplate = utilities.bicCodeTemplate;
        TokenFormattingTemplate<String, String> bnkAccNumTmplt = utilities.bankAccountNumberTemplate;
        Map<String, Currency> currencyMap = RefDataCollection.singleton().currencyMap;
        nsBdr
                .nostroID(idProvider.nextAsString())
                .nostroVersion(ConfigParams.FIRST_TEST_DATA_VERSION)
                .entityCode(entity.entityCode())
                .entityVersion(entity.entityVersion())
                .currCode(currency.currCode())
                .secondaryLedgerAccount(slaCodeProvider.nextAsString())
                .primary(this.isParentDefinition())
                .beneBic(entity.bicCode())
                .bankBic(bicCodeTemplate.apply("", getBicCodeTokenValues("BK"), "X", AffixPosition.SUFFIX, 2).toUpperCase())
                .bankAccount(bnkAccNumTmplt.apply("00", List.of(bnkAccNumGntr.nextAsString()), ""))
                .bankLine1(utilities.stringRefDataProvider.next(TestDataType.STATE_OR_CITY_OR_STREET_GENERIC_NAMES))
                .corrBic(utilities.isAnNthDefinition(10) ? bicCodeTemplate.apply("", getBicCodeTokenValues("CR"), "X", AffixPosition.SUFFIX, 2).toUpperCase() : null)
                .corrAccount(utilities.isAnNthDefinition(10) ? bnkAccNumTmplt.apply("", List.of(bnkAccNumGntr.nextAsString()), "00") : null)
                .corrLine1(utilities.isAnNthDefinition(10) ? utilities.stringRefDataProvider.next(TestDataType.STATE_OR_CITY_OR_STREET_GENERIC_NAMES) : null)
                .cutOffTime(currencyMap.get(currency.currCode()).cutOffTime())
                .cutInHoursOffset(utilities.rndmGntr.nextInt(2, 12))
                .paymentLimit(utilities.isAnNthDefinition(6) ? BigDecimal.valueOf(utilities.rndmGntr.nextLong(10000000)) : new BigDecimal("0"))
                .active(true)
                .entryTime(LocalDateTime.now())
        ;

        return this;
    }

    private List<String> getBicCodeTokenValues(String thirdTokenValue) {
        String currentId = utilities.idGenerator.currentAsString();
        // A CityOrState name is taken just to form a random 2 char String.
        String state = utilities.stringRefDataProvider.next(TestDataType.STATE_OR_CITY_OR_STREET_GENERIC_NAMES);
        ArrayList<String> bicCodeTokenValues = new ArrayList<>();
        bicCodeTokenValues.add(currentId);
        bicCodeTokenValues.add((state.charAt(0) + "" + state.charAt(state.length() - 1)).toUpperCase());
        bicCodeTokenValues.add(thirdTokenValue);
        return Collections.unmodifiableList(bicCodeTokenValues);
    }

    @Override
    public Nostro buildDefinition() {
        return nsBdr.build();
    }

    @Override
    protected NostroDefinition childDefinition(Nostro parent) {
        return new NostroDefinition(this.entity, this.currency, parent).withDefaults();
    }
}
