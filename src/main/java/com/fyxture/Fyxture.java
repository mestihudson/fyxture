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
  private static String datasource = null;
  private static String driver;
  private static String url;
  private static String user;
  private static String password;

  private static final String DELETE = "DELETE FROM %s;";
  private static final String SEQUENCE_ALTER = "ALTER SEQUENCE %s RESTART WITH 1;";
  private static final String ORACLE_SEQUENCE_DROP = "DROP SEQUENCE %s;";
  private static final String ORACLE_SEQUENCE_CREATE = "CREATE SEQUENCE %s MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH %s CACHE 20 NOORDER NOCYCLE;";
  private static final String COUNT = "SELECT COUNT(1) FROM %s";
  private static final String INSERT = "INSERT INTO %s (%s) VALUES (%s);";
  private static final String SELECT = "SELECT %s FROM %s WHERE %s;";

  public static void clear() throws Throwable {
    init();
    Map tables = m(get("config", "table"));
    String tablenames = "";
    String alter_sequences = "";
    for(Object key : tables.keySet()) {
      tablenames = tablenames.concat(fmt(DELETE, key));
      alter_sequences = sequences(key.toString());
    }
    String command = tablenames.concat(alter_sequences);
    for(String cmd : command.split(";")){
      logger.info(cmd);
      statement.execute(cmd);
    }
  }

  private static String sequences(String key) throws Throwable {
    String command = "";
    try{
      String sequence = s(get("config", fmt("table.%s.sequence.name", key)));
      if(datasource.equals("oracle")){
        command = command.concat(fmt(ORACLE_SEQUENCE_DROP, sequence));
        command = command.concat(fmt(ORACLE_SEQUENCE_CREATE, sequence, next(key)));
      }else{
        command = command.concat(fmt(SEQUENCE_ALTER, sequence));
      }
    }catch(Throwable t){}
    return command;
  }

  public static Integer count(String table) throws Throwable {
    init();
    String command = fmt(COUNT, table);
    ResultSet rs = statement.executeQuery(command);
    rs.next();
    return rs.getInt(1);
  }

  public static void insert(String table) throws Throwable {    
    insert(table, s(get("config", "common.table.default.descriptor")));
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
    String suffix = s(get("config", "common.table.suffix"));
    String tabledes = fmt("%s.%s", table, suffix);
    logger.info(tabledes);
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
    String commands = cat(fmt(INSERT, table, cols, vals), "\n", fmt(ORACLE_SEQUENCE_DROP, sequence(table)), "\n", fmt(ORACLE_SEQUENCE_CREATE, sequence(table), next(table)));
    for(String command : commands.split("\n")){
      command = command.split(";")[0];
      logger.info(command);
      statement.execute(command);
    }
  }

  private static String sequence(String table) throws Throwable {
    return s(get("config", fmt("table.%s.sequence.name", table)));
  }

  private static String next(String table) throws Throwable {
    return String.valueOf(count(table) + 1);
  }

  private static String cat(String initial, String... parts) {
    for(String part : parts){
      initial = initial.concat(part);
    }
    return initial;
  }

  public static void insert(final String table, final Pair... pairs) throws Throwable {
    String descriptor = s(get("config", "common.table.default.descriptor"));
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

    String command = fmt(SELECT, cols, table, conditions_clause);
    command = command.split(";")[0];
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

  public static Fyxture init() throws Throwable {
    return init(s(get("config","common.datasource.default")));
  }

  public static Fyxture init(String datasourcename) throws Throwable {
    if(instance == null){
      if(datasource == null){
        datasource = datasourcename;
      }
      instance = new Fyxture(
        s(get("config", fmt("datasource.%s.driver", datasource))),
        s(get("config", fmt("datasource.%s.url", datasource))),
        s(get("config", fmt("datasource.%s.user", datasource))),
        s(get("config", fmt("datasource.%s.password", datasource)))
      );
    }
    return instance;
  }

  private static String fmt(String format, Object... args) {
    return String.format(format, args);
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
