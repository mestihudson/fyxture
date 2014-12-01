package com.fyxture;

public class OracleTest extends FyxtureTest {
  public OracleTest() {
    super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@//vm:1521/xe", "fyxture", "fyxture", "oracle");
  }
}
