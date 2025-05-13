package io.alw.css.refdataloader.domain;

import io.alw.css.model.referencedata.*;

public final class CacheTableDefinition {
    private static CacheTableDefinition instance;

    public final String ENTITY = """
            CREATE TABLE entity(
            entityCode VARCHAR PRIMARY KEY,                                               -- { entityCode | nullable = false }
            entityVersion INT,                                                            -- { entityVersion | nullable = false, length = 10 }
            entityName VARCHAR,                                                           -- { entityName }
            currCode VARCHAR,                                                             -- { currCode }
            countryCode VARCHAR,                                                          -- { countryCode | length = 2, nullable = false }
            countryName VARCHAR,                                                          -- { countryName }
            bicCode VARCHAR,                                                              -- { bicCode }
            active BOOLEAN,                                                               -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP
            ) WITH "template=replicated, backups=0, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.ENTITY, EntityCache.Key.class.getName(), EntityCache.class.getName());

    public final String NOSTRO = """
            CREATE TABLE nostro(
            nostroId VARCHAR,                                                             -- { nostroID | nullable = false }
            nostroVersion INT,                                                            -- { nostroVersion | nullable = false, length = 10 }
            entityCode VARCHAR,                                                           -- { entityCode | nullable = false }
            entityVersion INT,                                                            -- { entityVersion | nullable = false, length = 10 }
            currCode VARCHAR,                                                             -- { currCode | nullable = false }
            secondaryLedgerAccount VARCHAR,                                               -- { secondaryLedgerAccount | nullable = false }
            isPrimary BOOLEAN,                                                            -- { primary | nullable = false, length = 1 | STRING }
            beneBic VARCHAR,                                                              -- { beneBic }
            bankBic VARCHAR,                                                              -- { bankBic }
            bankAccount VARCHAR,                                                          -- { bankAccount }
            bankLine1 VARCHAR,                                                            -- { bankLine1 }
            corrBic VARCHAR,                                                              -- { corrBic }
            corrAccount VARCHAR,                                                          -- { corrAccount }
            corrLine1 VARCHAR,                                                            -- { corrLine1 }
            cutOffTime TIME,                                                              -- { cutOffTime }
            cutInHoursOffset INT,                                                         -- { cutInHoursOffset | length = 10 }
            paymentLimit DECIMAL,                                                         -- { paymentLimit | scale = 5 }
            active BOOLEAN,                                                               -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP,
            PRIMARY KEY (nostroId, entityCode)
            ) WITH "template=replicated, backups=0, affinityKey=entityCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.NOSTRO, NostroCache.Key.class.getName(), NostroCache.class.getName());

    public final String BOOK = """
            CREATE TABLE book(
            bookCode VARCHAR,        											        -- { bookCode | nullable = false }
            bookVersion INT,      											            -- { bookVersion | nullable = false, length = 10 }
            bookName VARCHAR,                   										-- { bookName }
            entityCode VARCHAR,                 										-- { entityCode | nullable = false }
            entityVersion INT,                									    	-- { entityVersion | nullable = false, length = 10 }
            productLine VARCHAR,                										-- { productLine | nullable = false }
            division VARCHAR,                    										-- { division }
            superDivision VARCHAR,              										-- { superDivision }
            mainCluster VARCHAR,                     									-- { cluster }
            subCluster VARCHAR,                 										-- { subCluster }
            tradeGroup VARCHAR,                 										-- { tradeGroup }
            active BOOLEAN,                                                             -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP,
            PRIMARY KEY (bookCode, entityCode)
            ) WITH "template=replicated, backups=0, affinityKey=entityCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.BOOK, BookCache.Key.class.getName(), BookCache.class.getName());

    public final String COUNTERPARTY = """
            CREATE TABLE counterparty(
            counterpartyCode VARCHAR PRIMARY KEY,        					  			-- { counterpartyCode | nullable = false }
            counterpartyVersion INT,      						  				        -- { counterpartyVersion | nullable = false, length = 10 }
            counterpartyName VARCHAR,                                         			-- { counterpartyName }
            parent BOOLEAN,                                                             -- { parent | nullable = false, length = 1 | STRING }
            internal BOOLEAN,                                                           -- { internal | nullable = false, length = 1 | STRING }
            parentCounterpartyCode VARCHAR,                                  			-- { parentCounterpartyCode }
            counterpartyType VARCHAR,                                         			-- { counterpartyType }
            bicCode VARCHAR,                                                  			-- { bicCode }
            entityCode VARCHAR,                                               			-- { entityCode }
            addressLine1 VARCHAR,                                             			-- { addressLine1 }
            addressLine2 VARCHAR,                                             			-- { addressLine2 }
            city VARCHAR,                                                      			-- { city }
            state VARCHAR,                                                     			-- { state }
            country VARCHAR,                                                   			-- { country }
            region VARCHAR,                                                    			-- { region }
            active BOOLEAN,                                                             -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP
            ) WITH "template=replicated, backups=0, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.COUNTERPARTY, CounterpartyCache.Key.class.getName(), CounterpartyCache.class.getName());

    public final String CP_NETTING_PROFILE = """
            CREATE TABLE counterpartyNettingProfile(
            nettingProfileId BIGINT,        				                                   -- { nettingProfileID | nullable = false, length = 19 }
            nettingProfileVersion INT,   	                                                   -- { nettingProfileVersion | nullable = false, length = 10 }
            counterpartyCode VARCHAR,                                                          -- { counterpartyCode | nullable = false }
            counterpartyVersion INT,                                                           -- { counterpartyVersion | nullable = false, length = 10 }
            product VARCHAR,                                                                   -- { product | nullable = false }
            nettingType VARCHAR,                                                               -- { nettingType }
            netByParentCounterpartyCode BOOLEAN,                                               -- { netByParentCounterpartyCode | nullable = false, length = 1 | STRING }
            netForAnyEntity BOOLEAN,                                                           -- { netForAnyEntity | nullable = false, length = 1 | STRING }
            entityCode VARCHAR,                                                                -- { entityCode }
            active BOOLEAN,                                                                    -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP,
            PRIMARY KEY (nettingProfileId, counterpartyCode)
            ) WITH "template=replicated, backups=0, affinityKey=counterpartyCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.CP_NETTING_PROFILE, CounterpartyNettingProfileCache.Key.class.getName(), CounterpartyNettingProfileCache.class.getName());

    public final String CP_SLA_MAPPING = """
            CREATE TABLE counterpartySlaMapping(
            mappingId BIGINT,            										    -- { mappingID | length = 19, nullable = false }
            mappingVersion INT,       										        -- { mappingVersion | length = 10, nullable = false }
            counterpartyCode VARCHAR,                                              	-- { counterpartyCode | nullable = false }
            counterpartyVersion INT,                                                -- { counterpartyVersion | nullable = false }
            entityCode VARCHAR,                                                    	-- { entityCode }
            currCode VARCHAR,                                                      	-- { currCode }
            secondaryLedgerAccount VARCHAR,                                       	-- { secondaryLedgerAccount }
            active BOOLEAN,                                                         -- { active | length = 1, nullable = false | STRING }
            entryTime TIMESTAMP,
            PRIMARY KEY (mappingId, counterpartyCode)
            ) WITH "template=replicated, backups=0, affinityKey=counterpartyCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.CP_SLA_MAPPING, CounterpartySlaMappingCache.Key.class.getName(), CounterpartySlaMappingCache.class.getName());

    public final String SSI = """
            CREATE TABLE ssi(
            ssiId VARCHAR,                                                            -- { ssiID | nullable = false }
            ssiVersion INT,                                                           -- { ssiVersion | length = 10, nullable = false }
            counterpartyCode VARCHAR,                                                 -- { counterpartyCode | nullable = false }
            counterpartyVersion INT,                                                  -- { counterpartyVersion | nullable = false, length = 10 }
            currCode VARCHAR,                                                         -- { currCode | nullable = false, length = 3 }
            product VARCHAR,                                                          -- { product | nullable = false }
            isPrimary BOOLEAN,                                                        -- { primary | nullable = false, length = 1 | STRING }
            beneType VARCHAR,                                                         -- { beneType }
            bankBic VARCHAR,                                                          -- { bankBic }
            bankAccount VARCHAR,                                                      -- { bankAccount }
            bankLine1 VARCHAR,                                                        -- { bankLine1 }
            corrBic VARCHAR,                                                          -- { corrBic }
            corrAccount VARCHAR,                                                      -- { corrAccount }
            corrLine1 VARCHAR,                                                        -- { corrLine1 }
            active BOOLEAN,                                                           -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP,
            PRIMARY KEY (ssiId, counterpartyCode)
            ) WITH "template=replicated, backups=0, affinityKey=counterpartyCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.SSI, SsiCache.Key.class.getName(), SsiCache.class.getName());

    public final String COUNTRY = """
            CREATE TABLE country(
            countryCode VARCHAR PRIMARY KEY,                                                -- { @Id | countryCode | nullable = false, length = 2 }
            countryName VARCHAR,                                                            -- { countryName }
            region VARCHAR,                                                                 -- { region }
            entryTime TIMESTAMP
            ) WITH "template=replicated, backups=0, affinityKey=countryCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.COUNTRY, CountryCache.Key.class.getName(), CountryCache.class.getName());

    public final String CURRENCY = """
            CREATE TABLE currency(
            currCode VARCHAR PRIMARY KEY,            										-- { @Id | currCode | nullable = false, length = 3 }
            countryCode VARCHAR,                										    -- { countryCode | nullable = false, length = 2 }
            pmFlag BOOLEAN,                                                                 -- { pmFlag | nullable = false, length = 1 | STRING }
            cutOffTime TIME,                 										        -- { cutOffTime }
            active BOOLEAN,                                                                 -- { active | nullable = false, length = 1 | STRING }
            entryTime TIMESTAMP
            ) WITH "template=replicated, backups=0, affinityKey=currCode, cache_name=%s, key_type=%s, value_type=%s"
            """.formatted(IgniteCacheName.CURRENCY, CurrencyCache.Key.class.getName(), CurrencyCache.class.getName());

    private CacheTableDefinition() {
        Class<CacheTableDefinition> cacheTableDefinitionClass = CacheTableDefinition.class;
    }

    public static CacheTableDefinition get() {
        var query = """
                Idea to make mapping the result of sql query easy.
                
                Following has to be done by an annotation processor:
                
                // Define java records that model the DB tables like Currency, Entity etc and annotate them so that the annotation processor can read them
                record Currency(.....){}
                record Entity(.....){}
                
                // Annotation processor has to create a structure of type 'Table' for each of the java records that model a DB table
                // The 'Table' structure has the field records
                Table cur = CssRefdata.Currency();
                Table ent = CssRefdata.Entity();
                
                // Annotate these result records so that annotation processor can read them and create the interface/implementation of 'SqlMapper'
                record ResultRecord1(String entityCode, String bicCode, String currCode, LocalTime cutOffTime){}
                record ResultRecord2(String entityCode, String bicCode, String currCode, LocalTime cutOffTime){}
                
                // This interface should be generated or user defined? It is possible to do both ways
                interface SqlMapper { // This interface can contain all/several mappers
                    ResultRecord map(ResultSet rs, SelectableFields sfs, Class<ResultRecord1> resultRecordClazz);
                }
                
                var selectableFields = WithSelectFields() // [1], [4]
                                        .(ent.entityCode) // [2]
                                        .(ent.bicCode)
                                        .(ent.currCode)
                                        .(cur.cutOffTime().rawObjectParser(transformFunc))
                                        .table(ent.class, "ent") // [3]
                                        .table(cur.class, "cur")
                                        .build();
                
                var resultRecord = (ResultRecord)SqlMapper.map(resultSet, selectableFields, ResultRecord.class) // [5]
                
                selFields.entityCode();
                selFields.bicCode();
                selFields.currCode();
                selFields.cutOffTime();
                
                [1]: The select object will store the fields from above tables and will preserve the order
                [2]: The record class that represents the field/column of the java class which represents the table 'Entity'.
                     NOTE: This record class needs to be generated by reading the java class which represents the table 'Entity'.
                     This record class contains the following:
                        1) Ordinal of the field in the java class
                        2) The type of the field in the java class
                        3) The name of the field in the java class
                        4) Optional method 'rawObjectParser' which parses the 'java.lang.Object' of the ResultSet.
                            When this custom parser is defined, this custom parser will be used instead of the default parser that just casts the 'java.lang.Object' to the type of the field.
                [3]: The method that defines the short form of the tables to be used with fields. This is only to make the output query pleasant to read
                [4]: The object built 'selectableFields' has the following properties:
                        1) Object::toString which creates the string of the fields in comma separated format which can be used in SQL select statement
                [5]: Pass the resultSet object and 'selectableFields' to the mapper that returns a java record with the mapped values.
                     SqlMapper::map can be just a static method
                     The mapper must return a java record or a class with all the mapped fields as final
                """;

        if (instance == null) {
            synchronized (CacheTableDefinition.class) {
                if (instance == null) {
                    instance = new CacheTableDefinition();
                }
            }
        }

        return instance;
    }
}
