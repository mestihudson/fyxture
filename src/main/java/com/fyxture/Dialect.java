package com.fyxture;

import static com.fyxture.Utils.*;

import org.apache.log4j.Logger;

import java.util.List;

abstract class Dialect {
  private static Logger logger = Logger.getLogger(Dialect.class);

  private static final String COUNT = "SELECT COUNT(1) FROM [%s]";
  static final String DELETE = "DELETE FROM %s";
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

  void insert(InsertCommand ic) throws Throwable {
    String command = ic.toString();
    logger.debug(command);
    fyxture.execute(command);
  }

  String count_command() {
    return COUNT;
  }

  abstract void reset_sequence(String table) throws Throwable;
}
