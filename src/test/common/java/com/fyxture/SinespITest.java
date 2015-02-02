package com.fyxture;

import org.junit.Test;
import org.junit.Ignore;
import org.apache.log4j.Logger;

public class SinespITest {
  private static Logger logger = Logger.getLogger(SinespITest.class);

  @Test public void clear() throws Throwable {
    Fyxture.init("sinesp");
    Fyxture.clear();
  }
}
