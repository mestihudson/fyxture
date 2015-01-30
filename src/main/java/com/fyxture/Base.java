package com.fyxture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Base {
  private static Logger logger = Logger.getLogger(Base.class);
  private static Map<String, Connection> connections = new LinkedHashMap<String, Connection>();
  private static Map<String, List<String>> tables = new LinkedHashMap<String, List<String>>();

  private static Connection connection(String ds) throws Throwable {
    if(ds == null){
      return connection(Data.datasource());
    }else{
      logger.info(ds);
      if(!connections.containsKey(ds)){
        Class.forName(Data.driver(ds)).newInstance();
        connections.put(ds, DriverManager.getConnection(Data.url(ds), Data.user(ds), Data.password(ds)));
      }
    }
    return connections.get(ds);
  }

  public static List<String> tables() throws Throwable {
    return tables(null, "%");
  }

  public static List<String> tables(String ds, String pattern) throws Throwable {
    if(ds == null){
      return tables(Data.datasource(), pattern);
    }else{
    	if(!tables.containsKey(ds)){
      	List<String> all = new ArrayList<String>();
        ResultSet rs = connection(ds).getMetaData().getTables(null, Data.schema(ds), pattern, new String[] {"TABLE"});
        while(rs.next()){
          String table = rs.getString(3);
          if(!Data.clear_excludes(ds).contains(table)){
          	all.add(table);
          }
        }
        for(String name : all){
        	ResultSet ikrs = connection(ds).getMetaData().getImportedKeys(null, Data.schema(ds), name);
          int from = all.indexOf(name);
          while(ikrs.next()){
          	String importedTable = ikrs.getString(3);
          	int to = all.indexOf(importedTable);
          	if(from > to){
          		all.set(from, all.get(to));
          		all.set(to, name);
          	}
          }
        }
        tables.put(ds, all);
    	}
    	return tables.get(ds);
    }
  }
}
