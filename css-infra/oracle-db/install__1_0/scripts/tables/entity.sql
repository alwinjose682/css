CREATE TABLE entity(
entity_code VARCHAR2(5) NOT NULL,                                                   -- { entityCode | nullable = false }
entity_version NUMBER(10) NOT NULL,                                                 -- { entityVersion | nullable = false, length = 10 }
entity_name VARCHAR2(50),                                                           -- { entityName }
curr_code VARCHAR2(3),                                                              -- { currCode }
country_code VARCHAR2(5),                                                           -- { countryCode | length = 2, nullable = false }
country_name VARCHAR2(25),                                                          -- { countryName }
bic_code VARCHAR2(11),                                                              -- { bicCode }
active VARCHAR2(1) NOT NULL CONSTRAINT ent_active_chk CHECK(active IN('Y','N')),        -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                                                             -- { entryTime }
)
tablespace &DATA_TS
;

ALTER TABLE entity ADD CONSTRAINT ent_pk PRIMARY KEY(entity_code, entity_version) USING INDEX tablespace &INDEX_TS;
