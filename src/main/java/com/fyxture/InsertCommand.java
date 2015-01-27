package com.fyxture;

import static com.fyxture.Utils.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

class InsertCommand {
  private static Logger logger = Logger.getLogger(InsertCommand.class);

  static final String INSERT = "INSERT INTO %s (%s) VALUES (%s)";

  Fyxture fyxture;
  String datasource;
  String table;
  String descriptor;
  Pair [] pairs;
  List<String> columns = new ArrayList<String>();
  List<Object> values = new ArrayList<Object>();

  InsertCommand(Fyxture fyxture, String datasource, String table, String descriptor, Pair... pairs) throws Throwable {
    this.fyxture = fyxture;
    this.datasource = datasource;
    this.table = table;
    this.descriptor = descriptor;
    this.pairs = pairs;
    process();
  }

  private void process() throws Throwable {
    Map<String, Object> decoded = new LinkedHashMap<String, Object>();
    for(Pair pair : pairs){
      decoded.put(pair.key, pair.value);
    }
    logger.debug(decoded);
    String suffix = s(fyxture.get("config", "common.table.suffix"));
    Object o = fyxture.get(fmt("%s/%s.%s", datasource, table, suffix), descriptor);
    Map c = m(o);
    logger.debug(c);
    logger.debug(m(fyxture.get(datasource + "/" + table + ".table")));
    columns = new ArrayList<String>();
    values = new ArrayList<Object>();
    for(Object key : c.keySet()){
      columns.add(s(key));
      values.add(decoded.get(key) == null ? (c.get(key) == null ? null : c.get(key)) : decoded.get(key));
    }
  }

  public String toString() {
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
}
