package com.fyxture;

import static com.fyxture.Utils.*;

import org.apache.log4j.Logger;

import java.util.List;

class SQLServerDialect extends Dialect {
  private static Logger logger = Logger.getLogger(SQLServerDialect.class);

  private static final String SQLSERVER_SEQUENCE_ALTER = "DBCC CHECKIDENT ('%s', RESEED, %s)";

  SQLServerDialect(Fyxture fyxture) {
    super(fyxture);
  }

  void reset_sequence(String table) throws Throwable {
    fyxture.execute(fmt(SQLSERVER_SEQUENCE_ALTER, table, "1", "1"));
  }

  void insert(String table, List<String> columns, List<Object> values) {}
}
