package com.fyxture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Base {
  private static Logger logger = Logger.getLogger(Base.class);
  private static Map<String, Connection> connections = new LinkedHashMap<String, Connection>();
  private static Map<String, List<String>> tables = new LinkedHashMap<String, List<String>>();
  private static Map<String, List<String>> cleanables = new LinkedHashMap<String, List<String>>();

  private static Connection connection(String ds) throws Throwable {
    if(ds == null){
      return connection(Data.datasource());
    }else{
      logger.debug(ds);
      if(!connections.containsKey(ds)){
        Class.forName(Data.driver(ds)).newInstance();
        connections.put(ds, DriverManager.getConnection(Data.url(ds), Data.user(ds), Data.password(ds)));
      }
    }
    return connections.get(ds);
  }

  public static List<String> cleanables() throws Throwable {
  	return cleanables(null);
  }

  public static List<String> cleanables(String ds) throws Throwable {
  	if(ds == null) {
  		return cleanables(Data.datasource());
  	}else {
  		if(!cleanables.containsKey(ds)){
  			List<String> tables = tables(null, "%");
  			List<String> unclear = Data.unclear(ds);
  			List<String> result = new ArrayList<String>();
  			result.addAll(tables);
  			for(String table : tables){
  				for(String u : unclear) {
  					if(Pattern.compile(u).matcher(table).find()){
  						result.remove(table);
  					}
  				}
  			}
  			cleanables.put(ds, result);
  		}
      return cleanables.get(ds);
  	}
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
          all.add(table);
        }
        List<String> excludes = Data.excludes(ds);
        int count = 0;
        while(true){
          String table = all.get(count);
          boolean excluded = false;
          for(String item : excludes){
            if(Pattern.compile(item).matcher(table).find()){
              excluded = true;
              break;
            }
          }
          if(excluded){
            all.remove(table);
          }else{
            count++;
          }
          if(count == all.size()) {
          	break;
          }
        }
        count = 0;
        List<String> before = new ArrayList<String>();
        List<String> after = new ArrayList<String>();
        List<String> visited = new ArrayList<String>();
        boolean found = false;
        while(true){
        	String table = all.get(count);
        	ResultSet ekrs = connection(ds).getMetaData().getExportedKeys(null, Data.schema(ds), table);
         	while(ekrs.next()){
         		String importer = ekrs.getString(7);
         		if(all.indexOf(importer) > all.indexOf(table)){
         			before.clear();
         			before.addAll(all.subList(0, count));
         			after.clear();
         			after.addAll(all.subList(count, all.size()));
         			all.clear();
         			before.add(importer);
         			after.remove(importer);
         			all.addAll(before);
         			all.addAll(after);
         			found = true;
         			break;
         		}
         	}
         	if(found){
         		found = false;
         		continue;
         	}else{
           	if(!visited.contains(table)){
           		visited.add(table);
           	}         		
         	}
       		count++;
          if(count == all.size()) {
          	break;
          }
        }
        tables.put(ds, all);
    	}
    	return tables.get(ds);
    }
  }
}
