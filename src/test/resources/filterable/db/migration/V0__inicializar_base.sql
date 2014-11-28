CREATE USER IF NOT EXISTS SA SALT '6349ee1643b05f4a' HASH 'c0d971c592661a09010a960c6f5544c1d5a13b29347de97a5778f717cf455fb4' ADMIN;
CREATE SEQUENCE PUBLIC.SQ_ID_LIVRO START WITH 1 BELONGS_TO_TABLE;
CREATE CACHED TABLE PUBLIC.LIVRO (ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SQ_ID_LIVRO) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SQ_ID_LIVRO, VERSION BIGINT NOT NULL, ANO INTEGER NOT NULL, TITULO VARCHAR(255) NOT NULL);
ALTER TABLE PUBLIC.LIVRO ADD CONSTRAINT PUBLIC.PK_ID_LIVRO PRIMARY KEY(ID);