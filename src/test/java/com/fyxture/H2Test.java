package com.fyxture;

public class H2Test extends FyxtureTest {
  public H2Test() {
    super("org.h2.Driver", "jdbc:h2:target/fyxture", "sa", null, "h2");
  }

  protected String get_command_for_assert_current_value_of_sequence_is() {
    return "SELECT CURRVAL('SQ_ID_LIVRO')";
  }
}
