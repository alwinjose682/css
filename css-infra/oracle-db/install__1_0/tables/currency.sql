CREATE TABLE currency(
curr_code VARCHAR2(3) NOT NULL,            										    -- { @Id | currCode | nullable = false, length = 3 }
country_code VARCHAR2(5),                										    -- { countryCode | nullable = false, length = 2 }
pm_flag VARCHAR2(1) NOT NULL CONSTRAINT ccy_pm_chk CHECK(pm_flag IN('Y','N')),      -- { pmFlag | nullable = false, length = 1 | STRING }
cut_off_time TIMESTAMP(0),                 										    -- { cutOffTime }
active VARCHAR2(1) NOT NULL CONSTRAINT ccy_active_chk CHECK(active IN('Y','N')),    -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                   										    -- { entryTime }
)
tablespace &DATA_TS
;

ALTER TABLE currency ADD CONSTRAINT ccy_pk PRIMARY KEY(curr_code); -- No need for index as number of currencies are limited
