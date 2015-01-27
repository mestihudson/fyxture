package com.fyxture;

import static com.fyxture.Utils.*;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

class SQLServerDialect extends Dialect {
  private static Logger logger = Logger.getLogger(SQLServerDialect.class);

  private static final String SQLSERVER_SEQUENCE_ALTER = "DBCC CHECKIDENT ('%s', RESEED, %s)";
  private static final String SQLSERVER_IDENTITY_INSERT = "SET IDENTITY_INSERT %s %s";
  private static final String SQLSERVER_SEQUENCE_CURRENT = "SELECT IDENT_CURRENT('%s')";

  SQLServerDialect(Fyxture fyxture) {
    super(fyxture);
  }

  void reset_sequence(String table) throws Throwable {
    fyxture.execute(fmt(SQLSERVER_SEQUENCE_ALTER, table, "1", "1"));
  }

  void insert(InsertCommand ic) throws Throwable {
    String sequence = fyxture.sequence(ic.table, "column");
    if(sequence == null){
      super.insert(ic);
    }else{
      if(!ic.columns.contains(sequence)){
        ic.columns.add(sequence);
        ic.values.add(current(ic.table));
      }else{
        Object value = ic.values.get(ic.columns.indexOf(sequence));
        if(value == null){
          value = current(ic.table);
        }
      }
      fyxture.execute(fmt(SQLSERVER_IDENTITY_INSERT, ic.table, "ON"));
      super.insert(ic);
      fyxture.execute(fmt(SQLSERVER_IDENTITY_INSERT, ic.table, "OFF"));
    }
  }

  Integer current(String table) throws Throwable {
    ResultSet rs = fyxture.query(fmt(SQLSERVER_SEQUENCE_CURRENT, table));
    rs.next();
    return rs.getInt(1);
  }
}
