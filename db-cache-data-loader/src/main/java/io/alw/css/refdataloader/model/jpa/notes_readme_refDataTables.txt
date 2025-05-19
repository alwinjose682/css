--Notes on table design:
    - Tables/Entities:
        Country, Currency,
        Counterparty, Ssi, CounterpartyNettingProfile,
        Entity, Nostro, Book

    - Each item in all the tables, except country and currency, can have multiple versions in the same table itself.

    - About parent-child relationship
        Ideally, in a settlement system like CSS, parent-child relationships are not required.
        All Tables/Entities have id and version fields(example: nostroID and nostroVersion) just to have parent-child relationships so that I can persist the list of child entities using JPA
        Having such parent-child relationships will only make it difficult to maintain reference data in a backoffice system like CSS DB, which, in a real application, would local copy created based on the data obtained from a reference data system.

        But, In CSS there exists foreign keys from child to parent table, for the reference data tables.
        Due to this, following must be ensured whenever a new version of the parent entity is created:
            -- all child table records that references the existing record in the parent table must be modified to reference the new version of the record in the parent table.
            -- all versions prior to the new version of the item must have the column active='N'

--SSI, Book, CounterpartyNettingProfile table
    For simplicity, 'TradeType' is considered same as 'Product'

--Nostro table
    secondaryLedgerAccount(sla) must unique for each nostroID

--Book table
    TradeType is used as book's productLine

