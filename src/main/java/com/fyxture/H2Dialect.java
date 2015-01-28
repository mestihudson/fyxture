package com.fyxture;

import static com.fyxture.Utils.*;

class H2Dialect extends Dialect {
  private static final String H2_SEQUENCE_ALTER = "ALTER SEQUENCE %s RESTART WITH %s";

  H2Dialect(Fyxture fyxture) {
    super(fyxture);
  }

  void reset_sequence(String table) throws Throwable {
    fyxture.execute(fmt(H2_SEQUENCE_ALTER, Data.sequence(table), "1"));
  }
}
