package com.fyxture;

public class SQLServerTest extends FyxtureTest {
  public SQLServerTest() {
    super("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://w7:1433/fyxture", "sa", "s3nh@", "sqlserver");
  }

  protected void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    execute("SELECT SQ_ID_LIVRO.NEXTVAL FROM DUAL");
    super.assert_current_value_of_sequence_is(value + 1);
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return "SELECT SQ_ID_LIVRO.CURRVAL FROM DUAL";
  }
}
