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
    fyxture.execute(fmt(ORACLE_SEQUENCE_DROP, Data.sequence(table)));
    fyxture.execute(fmt(ORACLE_SEQUENCE_CREATE, Data.sequence(table), "1", "1"));
  }

  void insert(InsertCommand ic) throws Throwable {
    String column = Data.sequence(ic.table, "column");
    logger.debug(column);
    if(column != null && !ic.columns.contains(column)){
      fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, Data.sequence(ic.table)));
      ic.columns.add(column);
      ic.values.add(current(ic.table));
    }else{
      logger.debug(ic.values.get(ic.columns.indexOf(column)));
      if(ic.values.get(ic.columns.indexOf(column)) == null){
        logger.info(column);
        fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, Data.sequence(column)));
        ic.values.set(ic.columns.indexOf(column), current(ic.table));
      }else{
        Integer start = i(ic.values.get(ic.columns.indexOf(column)));
        logger.debug(start);
        fyxture.execute(fmt(ORACLE_SEQUENCE_DROP, Data.sequence(ic.table)));
        fyxture.execute(fmt(ORACLE_SEQUENCE_CREATE, Data.sequence(ic.table), start, start));
        fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, Data.sequence(ic.table)));
        ic.values.set(ic.columns.indexOf(column), current(ic.table));
      }
    }
    super.insert(ic);
  }

  private Integer current(String table) throws Throwable {
    ResultSet rs = fyxture.query(fmt(ORACLE_SEQUENCE_CURRENT, Data.sequence(table)));
    rs.next();
    return rs.getInt(1);
  }
}
