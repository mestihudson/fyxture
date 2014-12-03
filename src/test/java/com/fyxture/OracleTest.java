package com.fyxture;

public class OracleTest extends FyxtureTest {
  public OracleTest() {
    super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@//vm:1521/xe", "fyxture", "fyxture", "oracle");
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return "SELECT SQ_ID_LIVRO.CURRVAL FROM DUAL";
  }
}
