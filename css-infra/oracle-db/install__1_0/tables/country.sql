CREATE TABLE country(
country_code VARCHAR2(5) NOT NULL,                   -- { @Id | countryCode | nullable = false, length = 2 }
country_name VARCHAR2(25),                           -- { countryName }
region VARCHAR2(10),                                 -- { region }
entry_time TIMESTAMP(3)                              -- { entryTime }
)
tablespace &DATA_TS
;

ALTER TABLE country ADD CONSTRAINT ctry_pk PRIMARY KEY(country_code); -- No need for index as number of countries are limited
