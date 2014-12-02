package com.fyxture;

public class H2Test extends FyxtureTest {
  public H2Test() {
    super("org.h2.Driver", "jdbc:h2:target/fyxture", "sa", null, "h2");
  }

  protected void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    assert_current_value_of_sequence_is("SELECT CURRVAL('SQ_ID_LIVRO')", value);
  }
}
