CREATE SEQUENCE SQ_ID_LIVRO MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE TABLE LIVRO (ID NUMBER (10) NOT NULL, VERSION NUMBER(10) NOT NULL, ANO NUMBER(10) NOT NULL, TITULO VARCHAR2(255 BYTE) NOT NULL);
ALTER TABLE LIVRO ADD CONSTRAINT PK_ID_LIVRO PRIMARY KEY (ID);