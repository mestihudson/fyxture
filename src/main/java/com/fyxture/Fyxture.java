package com.fyxture;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;

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

  private static final String DELETE = "DELETE FROM %s;";
  private static final String SEQUENCE_ALTER = "ALTER SEQUENCE %s RESTART WITH 1;";
  private static final String COUNT = "SELECT COUNT(1) FROM %s;";
  private static final String INSERT = "INSERT INTO %s (%s) VALUES (%s);";
  private static final String SELECT = "SELECT %s FROM %s WHERE %s;";

  public static void clear() throws Throwable {
    init();
    Map tables = m(get("config", "tables"));
    String tablenames = "";
    String alter_sequences = "";
    for(Object key : tables.keySet()) {
      tablenames = tablenames.concat(String.format(DELETE, key));
      try{
        String sequence = s(get("config", String.format("tables.%s.sequence.name", key)));
        alter_sequences = alter_sequences.concat(String.format(SEQUENCE_ALTER, sequence));
      }catch(Throwable t){}
    }
    String command = tablenames.concat(alter_sequences);
    logger.info(command);
    statement.executeUpdate(command);
  }

  public static Integer count(String table) throws Throwable {
    init();
    String command = String.format(COUNT, table);
    ResultSet rs = statement.executeQuery(command);
    rs.next();
    return rs.getInt(1);
  }

  public static void insert(String table) throws Throwable {    
    String descriptor = s(get("config", "table.default.descriptor"));
    insert(table, descriptor);
  }

  public static void insert(String table, String descriptor) throws Throwable {
    insert(table, descriptor, new Pair[]{});
  }

  public static void insert(String table, String descriptor, Pair... pairs) throws Throwable {
    init();
    Map<String, Object> decoded = new LinkedHashMap<String, Object>();
    for(Pair pair : pairs){
      decoded.put(pair.key, pair.value);
    }
    String suffix = s(get("config", "table.suffix"));
    String tabledes = String.format("%s.%s", table, suffix);
    Map c = m(get(tabledes, descriptor));
    logger.info(c);
    String cols = "";
    String vals = "";
    for(Object key : c.keySet()){
      cols = cols.concat((cols.equals("") ? "" : ", ") + key);
      Object v = null;
      if(decoded.containsKey(key)){
        v = decoded.get(key);
      }else{
        v = c.get(key);
      }
      v = v instanceof String ? "'" + v + "'" : v;
      vals = vals.concat((vals.equals("") ? "" : ", ") + v);
    }
    String command = String.format(INSERT, table, cols, vals);
    statement.executeUpdate(command);
    logger.info(command);
  }

  public static void insert(final String table, final Pair... pairs) throws Throwable {
    String descriptor = s(get("config", "table.default.descriptor"));
    insert(table, descriptor, pairs);
  }

  public static List<Map<String, Object>> select(final String table, final Cols columns, final Where where) throws Throwable {
    init();
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    String cols = "*";
    for(String name : columns.names) {
      if(cols.equals("*")){
        cols = "1";
      }
      cols = cols + ", " + name;
    }

    String conditions_clause = where.clause == null ? "1=1" : where.clause;

    String command = String.format(SELECT, cols, table, conditions_clause);
    logger.info(command);
    ResultSet rs = statement.executeQuery(command);
    ResultSetMetaData md = rs.getMetaData();
    while(rs.next()){
      Map<String, Object> line = new LinkedHashMap<String, Object>();
      for(int i = 1; i <= md.getColumnCount(); i++){
        line.put(md.getColumnName(i), rs.getObject(i));
      }
      result.add(line);
    }
    logger.info(result);
    return result;
  }

  public static List<Map<String, Object>> select(final String table) throws Throwable {
    return select(table, cols(), where(null));
  }

  public static List<Map<String, Object>> select(final String table, final Cols cols) throws Throwable {
    return select(table, cols, where(null));
  }

  public static List<Map<String, Object>> select(final String table, final Where where) throws Throwable {
    return select(table, cols(), where);
  }

  private static String quote(Object v) {
    String result = "" + (v instanceof String ? "'" + v + "'" : v);
    return result;
  }

  public static Pair pair(String key, Object value) {
    return new Pair(key, value);
  }

  public static Where where(String clause) {
    return new Where(clause);
  }

  public static Cols cols(String... names) {
    return new Cols(names);
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

  private static Fyxture init() throws Throwable {
    if(instance == null){
      instance = new Fyxture(
        s(get("config","datasource.driver")), 
        s(get("config","datasource.url")),
        s(get("config","datasource.user")),
        s(get("config","datasource.password"))
      );
    }
    return instance;
  }

  private static Object load(String filename) throws Throwable {
    if(map.get(filename) == null) {
      String filepath = Fyxture.class.getClassLoader().getResource(filename.concat(".yml")).getPath();
      logger.info(filepath);
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
}
