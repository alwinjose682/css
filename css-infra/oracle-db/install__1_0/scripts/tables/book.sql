CREATE TABLE book(
book_code VARCHAR2(5) NOT NULL,        											-- { bookCode | nullable = false }
book_version NUMBER(10) NOT NULL,      											-- { bookVersion | nullable = false, length = 10 }
book_name VARCHAR2(25),                   										-- { bookName }
entity_code VARCHAR2(5),                 										-- { entityCode | nullable = false }
entity_version NUMBER(10),                										-- { entityVersion | nullable = false, length = 10 }
product_line VARCHAR2(15),                										-- { productLine | nullable = false }
division VARCHAR2(30),                    										-- { division }
super_division VARCHAR2(30),              										-- { superDivision }
main_cluster VARCHAR2(30),                     									-- { cluster }
sub_cluster VARCHAR2(30),                 										-- { subCluster }
trade_group VARCHAR2(30),                 										-- { tradeGroup }
active VARCHAR2(1) NOT NULL CONSTRAINT bk_active_chk CHECK(active IN('Y','N')),    -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                   										-- { entryTime }
);

ALTER TABLE book ADD CONSTRAINT bk_pk PRIMARY key(book_code, book_version);
ALTER TABLE book ADD CONSTRAINT bk_ent_fk FOREIGN KEY(entity_code, entity_version) REFERENCES entity(entity_code, entity_version);
--CREATE INDEX bk_cp_et_pl_dn_idx on book(entity_code, entity_version, product_line, division);
