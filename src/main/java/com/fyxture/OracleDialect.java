package com.fyxture;

import static com.fyxture.Utils.fmt;
import static com.fyxture.Utils.i;

import java.sql.ResultSet;
import java.util.Map;

import org.apache.log4j.Logger;

class OracleDialect extends Dialect {
  private static Logger logger = Logger.getLogger(OracleDialect.class);

  private static final String ORACLE_SEQUENCE_DROP = "DROP SEQUENCE %s";
  private static final String ORACLE_SEQUENCE_CREATE = "CREATE SEQUENCE %s MINVALUE %s MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH %s CACHE 20 NOORDER NOCYCLE";
  private static final String ORACLE_SEQUENCE_NEXT = "SELECT %s.NEXTVAL FROM DUAL";
  private static final String ORACLE_SEQUENCE_CURRENT = "SELECT %s.CURRVAL FROM DUAL";

  OracleDialect(Fyxture fyxture) {
    super(fyxture);
  }

  void reset(String table) throws Throwable {
  	Map sequences = Utils.m(Config.ds().sequences(table));
  	logger.info(sequences);
  	for(Object sequence : sequences.keySet()){
	    fyxture.execute(fmt(ORACLE_SEQUENCE_DROP, sequences.get(sequence)));
	    fyxture.execute(fmt(ORACLE_SEQUENCE_CREATE, sequences.get(sequence), "1", "1"));
  	}
  }

  void insert(InsertCommand ic) throws Throwable {
  	Map sequences = Utils.m(Config.ds().sequences(ic.table));
  	for(Object sequence : sequences.keySet()){
      String column = Utils.s(sequence);
      String seqobj = Utils.s(sequences.get(sequence));
      logger.debug(column);
      if(column != null){
      	if(!ic.columns.contains(column)){
      		fyxture.execute(fmt(ORACLE_SEQUENCE_NEXT, seqobj));
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
      }  		
  	}
    super.insert(ic);
  }

  private Integer current(String sequence) throws Throwable {
    ResultSet rs = fyxture.query(fmt(ORACLE_SEQUENCE_CURRENT, sequence));
    rs.next();
    return rs.getInt(1);
  }
}
