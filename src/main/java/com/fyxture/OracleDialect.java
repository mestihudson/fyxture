package com.fyxture;

import static com.fyxture.Utils.*;

import org.apache.log4j.Logger;

import java.util.List;
import java.sql.ResultSet;

class OracleDialect extends Dialect {
  private static Logger logger = Logger.getLogger(OracleDialect.class);

  private static final String ORACLE_SEQUENCE_DROP = "DROP SEQUENCE %s";
  private static final String ORACLE_SEQUENCE_CREATE = "CREATE SEQUENCE %s MINVALUE %s MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH %s CACHE 20 NOORDER NOCYCLE";
  private static final String ORACLE_SEQUENCE_NEXT = "SELECT %s.NEXTVAL FROM DUAL";
  private static final String ORACLE_SEQUENCE_CURRENT = "SELECT %s.CURRVAL FROM DUAL";

  OracleDialect(Fyxture fyxture) {
    super(fyxture);
  }

  void reset_sequence(String table) throws Throwable {
    fyxture.execute(fmt(ORACLE_SEQUENCE_DROP, fyxture.sequence(table)));
    fyxture.execute(fmt(ORACLE_SEQUENCE_CREATE, fyxture.sequence(table), "1", "1"));
  }

  void insert(String table, List<String> columns, List<Object> values) throws Throwable {
    String column = fyxture.sequence(table, "column");
    logger.debug(column);
    if(column != null && !columns.contains(column)){
      fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, fyxture.sequence(table)));
      columns.add(column);
      values.add(current(table));
    }else{
      logger.debug(values.get(columns.indexOf(column)));
      if(values.get(columns.indexOf(column)) == null){
        logger.info(column);
        fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, fyxture.sequence(column)));
        values.set(columns.indexOf(column), current(table));
      }else{
        Integer start = i(values.get(columns.indexOf(column)));
        logger.debug(start);
        fyxture.execute(fmt(ORACLE_SEQUENCE_DROP, fyxture.sequence(table)));
        fyxture.execute(fmt(ORACLE_SEQUENCE_CREATE, fyxture.sequence(table), start, start));
        fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, fyxture.sequence(table)));
        values.set(columns.indexOf(column), current(table));
      }
    }
    super.insert(table, columns, values);
  }

  private Integer current(String table) throws Throwable {
    ResultSet rs = fyxture.query(fmt(ORACLE_SEQUENCE_CURRENT, fyxture.sequence(table)));
    rs.next();
    return rs.getInt(1);
  }
}
