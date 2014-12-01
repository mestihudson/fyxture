package com.fyxture;

public class H2Test extends FyxtureTest {
  public H2Test() {
    super("org.h2.Driver", "jdbc:h2:target/fyxture", "sa", null, "h2");
  }
}
