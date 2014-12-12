package com.fyxture;

import static com.fyxture.Utils.*;

import org.apache.log4j.Logger;

import java.util.List;

abstract class Dialect {
  private static Logger logger = Logger.getLogger(Dialect.class);

  static final String DELETE = "DELETE FROM %s";
  static final String INSERT = "INSERT INTO %s (%s) VALUES (%s)";
  Fyxture fyxture;

  Dialect(Fyxture fyxture) {
    logger.info(fyxture);
    this.fyxture = fyxture;
  }

  void delete(String table) throws Throwable {
	  logger.info(fyxture);
    logger.info(table);
    fyxture.execute(fmt(DELETE, table));
  }

  void insert(String table, List<String> columns, List<Object> values) throws Throwable {
    String cols = "";
    String vals = "";
    for(String column : columns){
      Object value = values.get(columns.indexOf(column));
      cols = cat(cols, comma(cols) + column);
      vals = cat(vals, comma(vals) + quote(value));
    }
    String command = fmt(INSERT, table, cols, vals);
    logger.info(command);
    fyxture.execute(command);
  }

  abstract void reset_sequence(String table) throws Throwable;
}
