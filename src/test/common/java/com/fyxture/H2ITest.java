package com.fyxture;

public class H2ITest extends FyxtureITest {
  public H2ITest() {
    super("org.h2.Driver", "jdbc:h2:target/fyxture", "sa", null, "h2");
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return get_command_for_assert_current_value_of_sequence_is("SQ_ID_LIVRO");
  }

  protected String get_command_for_assert_current_value_of_sequence_is(String name) {
    return "SELECT CURRVAL('"+ name +"')";
  }

  protected String sequence_for(String name) {
    return "SQ_ID_" + name;
  }
}
