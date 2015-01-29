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
  	set_sequence(table, 0);
  }

  private void set_sequence(String table, Integer value) throws Throwable {
  	fyxture.execute(fmt(SQLSERVER_SEQUENCE_ALTER, table, String.valueOf(value)));
  }

  void insert(InsertCommand ic) throws Throwable {
    String sequence = Data.sequence(ic.table, "column");
    if(sequence == null){
      super.insert(ic);
    }else{
      if(!ic.columns.contains(sequence)){
        ic.columns.add(sequence);
        Integer curr = current(ic.table) + 1;
        set_sequence(ic.table, curr);
        ic.values.add(curr);
      }else{
        Object value = ic.values.get(ic.columns.indexOf(sequence));
        if(value == null){
          Integer curr = current(ic.table) + 1;
          set_sequence(ic.table, curr);
          value = curr;
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
    Integer result = rs.getInt(1);
    return result;
  }
}
