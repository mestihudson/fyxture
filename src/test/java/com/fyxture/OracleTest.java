package com.fyxture;

public class OracleTest extends FyxtureTest {
  public OracleTest() {
    super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@//fyxture:1521/xe", "fyxture", "fyxture", "oracle");
  }

  protected void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    execute("SELECT SQ_ID_LIVRO.NEXTVAL FROM DUAL");
    super.assert_current_value_of_sequence_is(value + 1);
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return "SELECT SQ_ID_LIVRO.CURRVAL FROM DUAL";
  }
}
