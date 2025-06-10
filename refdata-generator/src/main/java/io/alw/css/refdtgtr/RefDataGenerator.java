package io.alw.css.refdtgtr;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.alw.css.domain.cashflow.TradeType;
import io.alw.datagen.TestDataGeneratable;
import io.alw.css.domain.referencedata.*;
import io.alw.css.refdtgtr.config.Json;
import io.alw.css.refdtgtr.definitions.*;
import io.alw.css.refdtgtr.domain.CounterpartyType;
import io.alw.css.refdtgtr.domain.PreDefinedTestData;
import io.alw.css.refdtgtr.model.CounterpartyAndDependentData;
import io.alw.css.refdtgtr.model.EntityAndDependentData;
import io.alw.css.refdtgtr.model.GeneratedReferenceData;
import io.alw.css.refdtgtr.provider.RefDataCollection;
import io.alw.datagen.tokengenerator.AlphaNumericTokenGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

// TODO: Use a dependency injection framework

public class RefDataGenerator {
    private final RandomGenerator rndm;
    private final PreDefinedTestData preDefinedTestData;
    private final int numOfCPCreationsToGnrtSlaMapping;
    private final int multiplierNumForMinimumRequiredCounterpartyCnt;

    private RefDataGenerator(int numOfCPCreationsToGnrtSlaMapping, int multiplierNumForMinimumRequiredCounterpartyCnt) {
        this.rndm = RandomGenerator.getDefault();
        this.preDefinedTestData = PreDefinedTestData.singleton();
        this.numOfCPCreationsToGnrtSlaMapping = numOfCPCreationsToGnrtSlaMapping;
        this.multiplierNumForMinimumRequiredCounterpartyCnt = multiplierNumForMinimumRequiredCounterpartyCnt;
    }

    public static void main(String[] args) {
//        rdg.testAlphanumericDataGenerator();
        GeneratedReferenceData generatedReferenceData = generateAllReferenceData(5, 2);
    }

    /// [RefDataGenerator#numOfCPCreationsToGnrtSlaMapping] and [RefDataGenerator#multiplierNumForMinimumRequiredCounterpartyCnt] determines, partly, the number of reference data generated.
    ///
    /// Other configs that determine the number of reference data are not exposed. And out of the total number of the reference data generation, a small percentage is random
    public static GeneratedReferenceData generateAllReferenceData(int numOfCPCreationsToGnrtSlaMapping, int multiplierNumForMinimumRequiredCounterpartyCnt) {
        RefDataGenerator rdg = new RefDataGenerator(numOfCPCreationsToGnrtSlaMapping, multiplierNumForMinimumRequiredCounterpartyCnt);
        var entityAndDependentData = rdg.createEntityAndDependentData();
        List<Nostro> allSecondaryNostros = entityAndDependentData.stream()
                .flatMap(ed -> ed.nostros().stream())
                .filter(nstr -> !nstr.primary())
                .toList();
        List<CounterpartyAndDependentData> counterpartyAndDependentData = rdg.createCounterpartyAndDependentData(allSecondaryNostros);
        return new GeneratedReferenceData(rdg.preDefinedTestData.countries, rdg.preDefinedTestData.currencies, entityAndDependentData, counterpartyAndDependentData);
    }

    public List<Country> getPredefinedCountryList() {
        return preDefinedTestData.countries;
    }

    public List<Currency> getPredefinedCurrencyList() {
        return preDefinedTestData.currencies;
    }

    public List<EntityAndDependentData> createEntityAndDependentData() {
        List<EntityAndDependentData> entityAndDependentData = new ArrayList<>();
        for (var ent : RefDataCollection.singleton().entities) {
            Entity entity = (Entity) ent;
            List<Nostro> nostros = createNostroData(entity);
            entityAndDependentData.add(new EntityAndDependentData(entity, nostros));
        }
        return entityAndDependentData;
    }

    /// Generates primary and secondary nostro for each currency for the given entity
    /// There is only one dependent data for an entity which is nostro(primary and secondary)
    private List<Nostro> createNostroData(Entity entity) {
        int totalNostros = 0;
        List<Nostro> allNostros = new ArrayList<>();

        for (var currency : RefDataCollection.singleton().currencies) {
            List<Nostro> nostros = new NostroDefinition(entity, (Currency) currency)
                    .withDefaults()
                    .childDefinitions(rndm.nextInt(5, 10))
                    .buildWithChildDefinitions();
            allNostros.addAll(nostros);

            int numOfNostros = nostros.size();
            totalNostros += numOfNostros;
            //System.out.println("Number of nostros: " + numOfNostros);
//                printAsJson(nostros);
        }
        //System.out.println("Total number of nostros: " + totalNostros);

        return allNostros;
    }

    public List<CounterpartyAndDependentData> createCounterpartyAndDependentData(List<Nostro> secondaryNostros) {
        int totalCounterparties = 0;
        int cpSlaMappingGeneration = 0;
        boolean shouldGenerateCpSlaMapping = false;
        List<CounterpartyAndDependentData> counterpartyAndDependentData = new ArrayList<>();

        final int minimumRequiredCounterpartyCnt = Arrays.asList(true, false).size()
                + CounterpartyType.values().length
                + RefDataCollection.singleton().entities.size();

        for (int idx = 0; idx < minimumRequiredCounterpartyCnt * multiplierNumForMinimumRequiredCounterpartyCnt; idx++) {

            // Build Counterparty
            List<Counterparty> counterparties = new CounterpartyDefinition()
                    .withDefaults()
                    .internal(rndm.nextBoolean())
                    .childDefinitions(rndm.nextInt(0, 5))
                    .buildWithChildDefinitions();
            int numOfCps = counterparties.size();
            totalCounterparties += numOfCps;
            //System.out.println("Number of counterparties: " + numOfCps);
//            printAsJson(counterparties);

            cpSlaMappingGeneration += numOfCps;
            if (cpSlaMappingGeneration >= numOfCPCreationsToGnrtSlaMapping) {
                shouldGenerateCpSlaMapping = true;
                cpSlaMappingGeneration = 0;
            } else {
                shouldGenerateCpSlaMapping = false;
            }
            List<CounterpartyAndDependentData> cpAndDdList = createCounterpartyDependentData(counterparties, secondaryNostros, shouldGenerateCpSlaMapping);
            for (CounterpartyAndDependentData cpAndDd : cpAndDdList) {
//                printAsJson(cpAndDd.counterparty());
//                printAsJson(cpAndDd.ssis());
//                printAsJson(cpAndDd.counterpartyNettingProfiles());
                if (!cpAndDd.counterpartySlaMappings().isEmpty()) {
//                    printAsJson(cpAndDd.counterpartySlaMappings());
                }
            }
            counterpartyAndDependentData.addAll(cpAndDdList);
        }

        //System.out.println("Total number of counterparties: " + totalCounterparties);
        return counterpartyAndDependentData;
    }

    /// Creates dependent data for the given list of counterparty for **EACH** TradeType and Currency.
    /// What are the dependent data of a counterparty?:
    /// They are:
    ///  1) SSI (primary and secondary)
    /// 2) CounterpartyNettingProfile and
    /// 3) CounterpartySlaMapping
    private List<CounterpartyAndDependentData> createCounterpartyDependentData(List<Counterparty> counterparties, List<Nostro> secondaryNostros, final boolean shouldGenerateCpSlaMapping) {
        List<CounterpartyAndDependentData> counterpartyAndDependentDataList = new ArrayList<>();
        int numOfCps = 0;
        int totalSsis = 0;
        int totalCpnps = 0;
        int totalCpsms = 0;

        for (Counterparty cp : counterparties) {
            List<Ssi> ssis = new ArrayList<>();
            List<CounterpartyNettingProfile> cpnps = new ArrayList<>();

            // ForEach TradeType/Product, Build Ssi and CounterpartyNettingProfile
            for (var tradeType : TradeType.values()) {

                // Build Ssi
                for (var currency : PreDefinedTestData.singleton().currencies) {
                    List<Ssi> ssisForEachCurrency = new SsiDefinition(cp, currency, tradeType)
                            .withDefaults()
                            .childDefinitions(rndm.nextInt(2, 7))
                            .buildWithChildDefinitions();
                    ssis.addAll(ssisForEachCurrency);

                    ++numOfCps;
                    int numOfSsis = ssisForEachCurrency.size();
                    totalSsis += numOfSsis;
                }

                // Build CounterpartyNettingProfile
                List<CounterpartyNettingProfile> cpnpsForEachProduct = new CounterpartyNettingProfileDefinition(cp)
                        .withDefaults()
                        .product(tradeType)
                        .netForAnyEntity(rndm.nextBoolean())
                        .netByParentCounterpartyCode(rndm.nextBoolean())
                        .buildWithChildDefinitions();
                cpnps.addAll(cpnpsForEachProduct);

            }

            // Build CounterpartySlaMapping
            // Any nostro, irrespective of entity, can be used
            final List<CounterpartySlaMapping> cpsms;
            if (shouldGenerateCpSlaMapping) {
                cpsms = new CounterpartySlaMappingDefinition(cp, secondaryNostros.get(rndm.nextInt(0, secondaryNostros.size())))
                        .withDefaults()
                        .buildWithChildDefinitions();
            } else {
                cpsms = new ArrayList<>();
            }

            CounterpartyAndDependentData cpAndDd = new CounterpartyAndDependentData(cp, ssis, cpnps, cpsms);
            counterpartyAndDependentDataList.add(cpAndDd);

            int numOfCpnps = cpnps.size();
            totalCpnps += numOfCpnps;
            int numOfCpsms = cpsms.size();
            totalCpsms += numOfCpsms;
            //System.out.println("Number of counterparty dependent data: Ssi: " + numOfSsis + ", CPNetProf: " + numOfCpnps + ", CPSlaMap: " + numOfCpsms);


        }

        //System.out.println("Number of Counterparties: " + numOfCps + ". Number of dependent data: Ssi: " + totalSsis + ", CPNetProf: " + totalCpnps + ", CPSlaMap: " + totalCpsms);
        return counterpartyAndDependentDataList;
    }

    private <T extends TestDataGeneratable> void printAsJson(T testData) {
        try {
            String ftd = Json.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(testData);
            System.out.println(ftd);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends TestDataGeneratable> void printAsJson(List<T> testDataList) {
        for (TestDataGeneratable tdg : testDataList) {
            try {
                String ftd = Json.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(tdg);
                System.out.println(ftd);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void testAlphanumericDataGenerator() {
        final int totalTokens = 2000;
        AlphaNumericTokenGenerator tp = new AlphaNumericTokenGenerator(10, new char[]{'h', '1', 'x', '0', '0'});
        for (int idx = 0; idx < totalTokens; idx++) {
//            System.out.println(tp.nextAsString());
            for (char ch : tp.next()) {
                System.out.print(ch);
            }
            System.out.println("Total number of tokens: " + totalTokens);
        }
    }
}
