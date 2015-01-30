package com.fyxture;


public class SQLServerITest extends FyxtureITest {
  public SQLServerITest() {
    super("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://w7:1433/fyxture", "sa", "s3nh@", "sqlserver");
  }

  protected void create(Integer id, Integer version, Integer ano, String titulo) throws Throwable {
    execute("SET IDENTITY_INSERT LIVRO ON");
    super.create(id, version, ano, titulo);
    execute("SET IDENTITY_INSERT LIVRO OFF");
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return get_command_for_assert_current_value_of_sequence_is("LIVRO");
  }

  protected String get_command_for_assert_current_value_of_sequence_is(String name) {
    return "SELECT IDENT_CURRENT('" + name + "')";
  }

  protected void assert_current_value_of_sequence_is(Integer value, String name) throws Throwable {
    super.assert_current_value_of_sequence_is(value, name);
  }

  protected void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    assert_current_value_of_sequence_is(value, "LIVRO");
  }

  public void clear() throws Throwable {
    clear_db();
    execute("SET IDENTITY_INSERT LIVRO ON");
    execute("INSERT INTO LIVRO (ID, VERSION, ANO, TITULO) VALUES (1, 0, 2015, 'Livro')");
    execute("SET IDENTITY_INSERT LIVRO OFF");
    execute("SET IDENTITY_INSERT AUTOR ON");
    execute("INSERT INTO AUTOR (ID, VERSION, NOME) VALUES (1, 0, 'Autor')");
    execute("SET IDENTITY_INSERT AUTOR OFF");
    execute("INSERT INTO AUTOR_LIVRO (AUTOR_ID, LIVRO_ID) VALUES (1, 1)");
    Fyxture.clear();
    assert_cleaned();
  }
}
