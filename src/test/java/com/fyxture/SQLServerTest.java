package com.fyxture;

public class SQLServerTest extends FyxtureTest {
  public SQLServerTest() {
    super("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://w7:1433/fyxture", "sa", "s3nh@", "sqlserver");
  }

  protected void create(Integer id, Integer version, Integer ano, String titulo) throws Throwable {
    execute("SET IDENTITY_INSERT LIVRO ON");
    super.create(id, version, ano, titulo);
    execute("SET IDENTITY_INSERT LIVRO OFF");
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return "SELECT IDENT_CURRENT('LIVRO')";
  }
}
