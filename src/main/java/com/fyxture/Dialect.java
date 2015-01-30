package com.fyxture;

import static com.fyxture.Utils.fmt;

import org.apache.log4j.Logger;

abstract class Dialect {
  private static Logger logger = Logger.getLogger(Dialect.class);

  private static final String COUNT = "SELECT COUNT(1) FROM %s";
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

  public void clear(String table) throws Throwable {
    delete(table);
    if(!Utils.m(Config.ds().sequences(table)).isEmpty()){
    	reset(table);
    }
  }

  abstract void reset(String table) throws Throwable;
}
