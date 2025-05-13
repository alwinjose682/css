CREATE TABLE data_load_status(
id NUMBER(10) GENERATED ALWAYS AS IDENTITY(START WITH 1 INCREMENT BY 1),            -- { @Id }
load_type VARCHAR2(10),                                                             -- { loadType | STRING }
start_time TIMESTAMP(3),                                                            -- { startTime }
end_time TIMESTAMP(3),                                                              -- { endTime }
status VARCHAR2(15)                                                                 -- { status }
)
tablespace &DATA_TS
;

--ALTER TABLE data_load_status MODIFY id NUMBER(10) <....>;
