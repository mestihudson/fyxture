DELETE FROM AUTOR_LIVRO;
DELETE FROM AUTOR;
DELETE FROM LIVRO;
DBCC CHECKIDENT ('AUTOR', RESEED, 0);
DBCC CHECKIDENT ('LIVRO', RESEED, 0);
