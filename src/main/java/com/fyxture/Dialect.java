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
    logger.debug(fyxture);
    this.fyxture = fyxture;
  }

  void delete(String table) throws Throwable {
    logger.debug(fyxture);
    logger.debug(table);
    fyxture.execute(fmt(DELETE, table));
  }

  void insert(String table, List<String> columns, List<Object> values) throws Throwable {
    String command = insert_command(table, columns, values);
    logger.debug(command);
    fyxture.execute(command);
  }

  String insert_command(String table, List<String> columns, List<Object> values) throws Throwable {
    String cols = "";
    String vals = "";
    for(String column : columns){
      Object value = values.get(columns.indexOf(column));
      logger.debug(column);
      logger.debug(value);
      cols = cat(cols, comma(cols) + column);
      vals = cat(vals, comma(vals) + quote(value));
    }
    return fmt(INSERT, table, cols, vals);
  }

  abstract void reset_sequence(String table) throws Throwable;
}
