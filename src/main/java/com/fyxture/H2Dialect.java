package com.fyxture;

import static com.fyxture.Utils.*;

import java.util.Map;

import org.apache.log4j.Logger;

class H2Dialect extends Dialect {
	private static Logger logger = Logger.getLogger(H2Dialect.class);
  private static final String H2_SEQUENCE_ALTER = "ALTER SEQUENCE %s RESTART WITH %s";

  H2Dialect(Fyxture fyxture) {
    super(fyxture);
  }

  void reset(String table) throws Throwable {
  	Map sequences = Utils.m(Config.ds().sequences(table));
  	logger.info(sequences);
  	for(Object sequence : sequences.keySet()){
  		fyxture.execute(fmt(H2_SEQUENCE_ALTER, sequences.get(sequence), "1"));
  	}
  }
}
