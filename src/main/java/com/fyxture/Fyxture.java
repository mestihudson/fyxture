package com.fyxture;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class Fyxture {
  private static Logger logger = Logger.getLogger(Fyxture.class);
  private static Map<String, Object> map = new LinkedHashMap<String, Object>();
  private static Fyxture instance;
  private static Connection connection = null;
  private static Statement statement = null;
  private static String driver;
  private static String url;
  private static String user;
  private static String password;
  private Object entity = null;
  private String entityname = null;
  private String entityfilter;

  private static final String DELETE = "DELETE FROM %s;";
  private static final String SEQUENCE_DROP = "DROP SEQUENCE %s;";
  private static final String SEQUENCE_CREATE = "CREATE SEQUENCE %s START WITH 1 BELONGS_TO_TABLE;";
  private static final String SEQUENCE_ALTER = "ALTER SEQUENCE %s RESTART WITH 1;";
  private static final String COUNT = "SELECT COUNT(1) FROM %s;";
  private static final String INSERT = "INSERT INTO %s (%s) VALUES (%s);";

  public static void clear() throws Throwable {
    init();
    Map entities = m(get("init.cfg", "entities"));
    String tables = "";
    String drop_sequences = "";
    String create_sequences = "";
    String alter_sequences = "";
    for(Object key : entities.keySet()){
      tables = tables.concat(String.format(DELETE, key));
      try{
        String sequence = s(get("init.cfg", String.format("entities.%s.sequence.name", key)));
        alter_sequences = alter_sequences.concat(String.format(SEQUENCE_ALTER, sequence));
      }catch(Throwable t){}
    }
    String command = tables.concat(alter_sequences);
    logger.info(command);
    statement.executeUpdate(command);
  }

  public static Integer count(String entity) throws Throwable {
    init();
    String command = String.format(COUNT, entity);
    ResultSet rs = statement.executeQuery(command);
    rs.next();
    return rs.getInt(1);
  }


  public static void insert(String entity) throws Throwable {    
    String descriptor = s(get("init.cfg", "entity.default.descriptor"));
    insert(entity, descriptor);
  }

  public static void insert(String entity, String descriptor) throws Throwable {
    insert(entity, descriptor, new Pair[]{});
  }

  public static void insert(String entity, String descriptor, Pair... pair) throws Throwable {
    init();
    String suffix = s(get("init.cfg", "entity.suffix"));
    String entitydes = String.format("%s.%s", entity, suffix);
    Map c = m(get(entitydes, descriptor));
    logger.info(c);
    String cols = "";
    String vals = "";
    for(Object key : c.keySet()){
      cols = cols.concat((cols.equals("") ? "" : ", ") + key);
      Object v = c.get(key);
      v = v instanceof String ? "'" + v + "'" : v;
      vals = vals.concat((vals.equals("") ? "" : ", ") + v);
    }
    String command = String.format(INSERT, entity, cols, vals);
    statement.executeUpdate(command);
    logger.info(command);
  }

  public static void insert(final String entity, final Pair... pairs) throws Throwable {
    String descriptor = s(get("init.cfg", "entity.default.descriptor"));
    insert(entity, descriptor, pairs);
  }

  public static Pair pair(String key, Object value) {
    return new Pair(key, value);
  }

  private static class Pair {
    String key;
    Object value;

    Pair(String key, Object value) {
      this.key = key;
      this.value = value;
    }
  } 

  private Fyxture(String driver, String url, String user, String password) throws Throwable {
    this.driver = driver;
    this.url = url;
    this.user = user;
    this.password = password;
    Class.forName(this.driver).newInstance();
    this.connection = DriverManager.getConnection(this.url, this.user, this.password);
    this.statement = this.connection.createStatement();
  }

  public static Fyxture init() throws Throwable {
    if(instance == null){
      instance = new Fyxture(
        s(get("init.cfg","ds.dev.driver")), 
        s(get("init.cfg","ds.dev.url")),
        s(get("init.cfg","ds.dev.user")),
        s(get("init.cfg","ds.dev.password"))
      );
    }
    return instance;
  }

  private static Object load(String filename) throws Throwable {
    if(map.get(filename) == null) {
      String filepath = "src/test/resources/filterable/" + filename + ".yml";
      InputStream input = new FileInputStream(new File(filepath));
      Yaml yaml = new Yaml();
      map.put(filename, yaml.load(input));
    }
    return map.get(filename);
  }

  private static Object get(String filename, String... path) throws Throwable {
    Object o = load(filename);
    if(path.length == 0){
      return o;
    }else{
      return path.length > 1 ? seg(o, path) : ges(o, path[0]);
    }
  }

  private static Object ges(Object o, String path) {
	  return seg(o, path.split("\\."));
  }

  private static Object seg(Object o, String... p) {
    Object no = o instanceof Map ? m(o).get(p[0]) : o;
    try{
      return seg(no, extract(p, 0));
    }catch(Throwable t){
      return no;
    }
  }

  private static String [] extract(String [] a, int position) {
    List<String> n = new ArrayList<String>(Arrays.asList(a));
    n.remove(position);
    String [] re = new String[n.size()];
    for(String e : n){
    	re[n.indexOf(e)] = e;
    }
    return re;
  }

  private static String s(Object v) {
    return ((String)v);
  }

  private static Map m(Object v) {
    return ((Map)v);
  }

  public Fyxture entity(String name, String filter) throws Throwable {
  	entityname = name;
  	entityfilter = filter;
  	entity = m(get(name.concat(".ent"))).get(filter);
  	return instance;
  }

  public Fyxture entity(String name) throws Throwable {
	 return entity(name, "default");
  }

  public Fyxture insert1() throws Throwable {
  	String command = String.format("INSERT INTO %s (%s) VALUES (%s);", entityname, columns(), values());
  	logger.info(command);
  	return instance;
  }

  private String values() throws Throwable {
  	String r = "";
  	for(Object k : m(entity).keySet()){
  		Object v = m(entity).get(k);
  		v = v instanceof String ? "'" + v + "'" : v;
  		String c = s(get("init.cfg", "entities." + entityname + ".sequence.column"));
  		String n = s(get("init.cfg", "entities." + entityname + ".sequence.name"));
  		v = v == null ? (c.equals(k.toString()) ? n.concat(".nextval") : v) : v;
  		r += r.length() == 0 ? v : ",".concat(v.toString());
  	}
  	return r;
  }

  private String columns() {
  	String r = "";
  	for(Object k : m(entity).keySet()){
  		r += r.length() == 0 ? k : ",".concat(k.toString());
  	}
  	return r;
  }
}
