package com.fyxture;

public class OracleITest extends FyxtureITest {
  public OracleITest() {
    super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@//fyxture:1521/xe", "fyxture", "fyxture", "oracle");
  }

  protected void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    assert_current_value_of_sequence_is(value, "SQ_ID_LIVRO");
  }

  protected void assert_current_value_of_sequence_is(Integer value, String name) throws Throwable {
    execute("SELECT " + name + ".NEXTVAL FROM DUAL");
    super.assert_current_value_of_sequence_is(value + 1, name);
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return get_command_for_assert_current_value_of_sequence_is("SQ_ID_LIVRO");
  }

  protected String get_command_for_assert_current_value_of_sequence_is(String name) {
    return "SELECT " + name + ".CURRVAL FROM DUAL";
  }

  protected String sequence_for(String name) {
    return "SQ_ID_" + name;
  }
}
