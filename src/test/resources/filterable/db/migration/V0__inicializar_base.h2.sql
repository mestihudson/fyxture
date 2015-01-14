--------
--CREATE USER IF NOT EXISTS SA SALT '6349ee1643b05f4a' HASH 'c0d971c592661a09010a960c6f5544c1d5a13b29347de97a5778f717cf455fb4' ADMIN;
--CREATE SEQUENCE PUBLIC.SQ_ID_LIVRO START WITH 1 BELONGS_TO_TABLE;
--CREATE CACHED TABLE PUBLIC.LIVRO (ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SQ_ID_LIVRO) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SQ_ID_LIVRO, VERSION BIGINT NOT NULL, ANO INTEGER NOT NULL, TITULO VARCHAR(255) NOT NULL);
--ALTER TABLE PUBLIC.LIVRO ADD CONSTRAINT PUBLIC.PK_ID_LIVRO PRIMARY KEY(ID);
--------
--CREATE USER IF NOT EXISTS SA SALT '1cbdc247540638cb' HASH 'a13bae057f7206f795c61de99ee62470a7b7aa8c9c506ec421cb00faf9025822' ADMIN;
--CREATE SEQUENCE PUBLIC.SQ_ID_LIVRO START WITH 1 BELONGS_TO_TABLE;
--CREATE SEQUENCE PUBLIC.SQ_ID_AUTOR START WITH 1 BELONGS_TO_TABLE;
--CREATE TABLE PUBLIC.AUTOR (ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SQ_ID_AUTOR) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SQ_ID_AUTOR, VERSION BIGINT NOT NULL, NOME VARCHAR(255) NOT NULL);
--ALTER TABLE PUBLIC.AUTOR ADD CONSTRAINT PUBLIC.PK_ID_AUTOR PRIMARY KEY(ID);
--CREATE TABLE PUBLIC.LIVRO (ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SQ_ID_LIVRO) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SQ_ID_LIVRO, VERSION BIGINT NOT NULL, ANO INTEGER NOT NULL, AUTOR_ID BIGINT NOT NULL, TITULO VARCHAR(255) NOT NULL);
--ALTER TABLE PUBLIC.LIVRO ADD CONSTRAINT PUBLIC.PK_ID_LIVRO PRIMARY KEY(ID);
--ALTER TABLE PUBLIC.LIVRO ADD CONSTRAINT PUBLIC.FK_AUTOR_ID FOREIGN KEY(AUTOR_ID) REFERENCES PUBLIC.AUTOR(ID) NOCHECK;
--------
create user if not exists sa salt '270a5f191b324569' hash 'c1137959c50b340b7b1cf851cc58952f2197ea187490aaf988046ffc39c07b13' admin;
create sequence public.sq_id_livro start with 1 belongs_to_table;
create sequence public.sq_id_autor start with 1 belongs_to_table;
create table public.livro (id bigint default (next value for public.sq_id_livro) not null null_to_default sequence public.sq_id_livro, version bigint not null, ano integer not null, titulo varchar(255) not null);
create table public.autor_livro (autor_id bigint, livro_id bigint);
create table public.autor (id bigint default (next value for public.sq_id_autor) not null null_to_default sequence public.sq_id_autor, version bigint not null, nome varchar(255) not null);
alter table public.livro add constraint public.pk_id_livro primary key(id);
alter table public.autor_livro add constraint public.fk_autor_id foreign key(autor_id) references public.autor(id) nocheck;
alter table public.autor_livro add constraint public.fk_livro_id foreign key(livro_id) references public.livro(id) nocheck;
alter table public.autor add constraint public.pk_id_autor primary key(id);
--------