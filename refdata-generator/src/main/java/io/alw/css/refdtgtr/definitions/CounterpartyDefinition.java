package io.alw.css.refdtgtr.definitions;

import io.alw.css.domain.referencedata.Counterparty;
import io.alw.css.domain.referencedata.CounterpartyBuilder;
import io.alw.css.domain.referencedata.Entity;
import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.css.refdtgtr.domain.CounterpartyType;
import io.alw.datagen.definition.BaseDefinition;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.css.refdtgtr.model.CountryStateCurrency;
import io.alw.datagen.model.AffixPosition;
import io.alw.css.refdtgtr.model.TestDataType;
import io.alw.datagen.tokengenerator.BinaryStringTokenGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Child counterparties will be created by inheriting following fields from primary definition:
/// - internal
/// - counterpartyType
/// - entityCode
/// - primary
/// - parentCounterpartyCode
///
/// It is valid for a single entity to have multiple counterparty codes
///
/// Note: Creating child counterparty is optional
public final class CounterpartyDefinition extends BaseDefinition<Counterparty> {
    private final CounterpartyDefinitionUtilities utilities;
    private final CounterpartyBuilder cptyBdr;
    private final CountryStateCurrency countryStateCurrency;

    public CounterpartyDefinition() {
        this(null, null);
    }

    /// childDefinitions will be created by inheriting following fields from primary definition:
    /// - internal
    /// - counterpartyType
    /// - entityCode
    /// - primary
    /// - parentCounterpartyCode
    private CounterpartyDefinition(CountryStateCurrency countryStateCurrency, Counterparty parent) {
        super(parent);
        this.utilities = CounterpartyDefinitionUtilities.singleton();
        this.cptyBdr = CounterpartyBuilder.builder();
        // Below is to ensure that all child CPs have the same Entity and Entity related data
        this.countryStateCurrency = countryStateCurrency == null
                ? (CountryStateCurrency) utilities.testDataProvider().next(TestDataType.DATA_SET__COUNTRY_STATE_CURRENCY)
                : countryStateCurrency;
    }

    @Override
    public CounterpartyDefinition withDefaults() {
        BinaryStringTokenGenerator<String, Long> idProvider = utilities.idProvider();
        TokenFormattingTemplate<String, String> simpleConcatenatingTemplate = utilities.simpleConcatenatingTemplate();
        List<String> idValues = idProvider.next();
        String idVariablePart = idProvider.tokenProvider2().currentAsString();

        cptyBdr
                .counterpartyCode(simpleConcatenatingTemplate.apply("", idValues, "0", AffixPosition.PREFIX_OF_LAST_TOKEN, 2))
                .counterpartyVersion(ConfigParams.FIRST_TEST_DATA_VERSION)
                .counterpartyName(simpleConcatenatingTemplate.apply("_", idValues, "name"));
        setParentAndParentCounterpartyCode();
        cptyBdr
                .addressLine1(simpleConcatenatingTemplate.apply("_", List.of(idVariablePart), "add_1_", AffixPosition.PREFIX, 1))
                .addressLine2(simpleConcatenatingTemplate.apply("_", List.of(idVariablePart), "add_2_", AffixPosition.PREFIX, 1))
                .city(simpleConcatenatingTemplate.apply("_", List.of(idVariablePart, countryStateCurrency.state()), ""))
                .state(simpleConcatenatingTemplate.apply("_", List.of(countryStateCurrency.state()), ""))
                .country(simpleConcatenatingTemplate.apply("_", List.of(countryStateCurrency.country().countryName()), ""))
                .region(simpleConcatenatingTemplate.apply("_", List.of(countryStateCurrency.country().countryCode()), ""))
                .active(true)
                .entryTime(LocalDateTime.now());
        return this;
    }

    private void setBicCode() {
        if (cptyBdr.counterpartyType().equals(CounterpartyType.NON_FINANCIAL.name())) {
            cptyBdr.bicCode(null);
        } else {
            List<String> bicCodeTokenValues = getBicCodeTokenValues();
            TokenFormattingTemplate<String, String> bicCodeTemplate = utilities.bicCodeTemplate();
            cptyBdr.bicCode(bicCodeTemplate.apply("", bicCodeTokenValues, "X", AffixPosition.SUFFIX, 2));
        }
    }

    private List<String> getBicCodeTokenValues() {
        List<String> currentTokenValues = utilities.idProvider().current();
        String state = countryStateCurrency.state();
        ArrayList<String> bicCodeTokenValues = new ArrayList<>(currentTokenValues);
        bicCodeTokenValues.add((state.charAt(0) + "" + state.charAt(state.length() - 1)).toUpperCase());
        bicCodeTokenValues.add(countryStateCurrency.country().region());
        return Collections.unmodifiableList(bicCodeTokenValues);
    }

    private void setParentAndParentCounterpartyCode() {
        if (isParentDefinition()) {
            cptyBdr.parent(true);
            cptyBdr.parentCounterpartyCode(null);
        } else {
            cptyBdr.parent(false);
            cptyBdr.parentCounterpartyCode(parent.counterpartyCode());
        }
    }

    /// If counterparty is internal, then entityCode, counterpartyType and bicCode needs to match the internal property
    public CounterpartyDefinition internal(boolean internal) {
        cptyBdr.internal(internal);
        if (internal) {
            setEntityCode();
        }
        setCounterpartyType(internal);
        setBicCode();
        return this;
    }

    private void setCounterpartyType(boolean internal) {
        if (!isParentDefinition()) {
            cptyBdr.counterpartyType(parent.counterpartyType());
        } else {
            if (internal) {
                cptyBdr.counterpartyType(CounterpartyType.BANK.name());
            } else {
                cptyBdr.counterpartyType(utilities.counterpartyTypeProvider().next(TestDataType.COUNTERPARTY_TYPE));
            }
        }
    }

    private void setEntityCode() {
        if (cptyBdr.internal()) {
            if (isParentDefinition()) {
                Entity entity = (Entity) utilities.testDataProvider().next(TestDataType.ENTITY);
                cptyBdr.entityCode(entity.entityCode());
            } else {
                cptyBdr.entityCode(parent.entityCode());
            }
        }

    }

    @Override
    public Counterparty buildDefinition() {
        return cptyBdr.build();
    }

    @Override
    protected CounterpartyDefinition childDefinition(Counterparty parent) {
        return new CounterpartyDefinition(this.countryStateCurrency, parent)
                .withDefaults()
                .internal(cptyBdr.internal());
    }
}
