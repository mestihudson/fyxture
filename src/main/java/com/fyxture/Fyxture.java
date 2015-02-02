package com.fyxture;

import static com.fyxture.Utils.cat;
import static com.fyxture.Utils.fmt;
import static com.fyxture.Utils.i;
import static com.fyxture.Utils.list;
import static com.fyxture.Utils.m;
import static com.fyxture.Utils.s;
import static com.fyxture.Utils.splitrim;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Fyxture {
  private static Logger logger = Logger.getLogger(Fyxture.class);
  private static Fyxture instance;
  private static Connection connection = null;
  private static Statement statement = null;
  private static String datasource = null;
  private static String driver;
  private static String url;
  private static String user;
  private static String password;
  private static Dialect dialect;
  private static final String SELECT = "SELECT %s FROM %s WHERE %s";
  private static final String NL = "\n";

  public static Integer count(String table) throws Throwable {
    init();
    ResultSet rs = query(fmt(dialect.count_command(), table));
    rs.next();
    return rs.getInt(1);
  }

  public static Fyxture insert(String table) throws Throwable {    
    insert(table, Data.ordinary());
    return instance;
  }

  public static Fyxture insert(String table, String descriptor) throws Throwable {
    insert(table, descriptor, new Pair[]{});
    return instance;
  }

  public static Fyxture insert(String table, String descriptor, Pair... pairs) throws Throwable {
    init();
    dialect.insert(insertCommand(datasource, table, descriptor, pairs));
    return instance;
  }

  public static void clear() throws Throwable {
    init();
    List<String> tables = Base.cleanables();
    for(String table : tables){
      dialect.clear(table);
    }
  }

  public static Fyxture insert(final String table, final Pair... pairs) throws Throwable {
    String descriptor = Data.ordinary();
    insert(table, descriptor, pairs);
    return instance;
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
    logger.debug(command);
    ResultSet rs = statement.executeQuery(command);
    ResultSetMetaData md = rs.getMetaData();
    while(rs.next()){
      Map<String, Object> line = new LinkedHashMap<String, Object>();
      for(int i = 1; i <= md.getColumnCount(); i++){
        line.put(md.getColumnName(i), rs.getObject(i));
      }
      result.add(line);
    }
    logger.debug(result);
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

  public static Pair pair(String key, Object value) {
    return new Pair(key, value);
  }

  public static Where where(String clause) {
    return new Where(clause);
  }

  public static Cols cols(String... names) {
    return new Cols(names);
  }

  public static InsertCommand insertCommand(String datasource, String table, String descriptor, Pair... pairs) throws Throwable {
    return new InsertCommand(datasource, table, descriptor, pairs);
  }

  public static Fyxture init() throws Throwable {
    String ds = datasource == null ? Data.datasource() : datasource;
    logger.debug(ds);
    return init(ds);
  }

  public static Fyxture init(String datasourcename) throws Throwable {
    logger.debug(datasourcename);
    if(!datasourcename.equals(datasource)){
      datasource = datasourcename;
      Data.datasource(datasourcename);
      instance = getInstance();
    }
    logger.debug(instance);
    return instance;
  }

  public static Fyxture verify(String name) throws Throwable {
    init();
    logger.debug(name);
    Object o = Data.verify(name);
    if(o instanceof String) {
      return verify(s(o));
    }
    for(Object p : m(o).keySet()){
      Object v = m(o).get(p);
      String sp = s(p);

      String pattern = "count~(";
      Integer index = sp.indexOf(pattern);
      if(index > -1){
        List c = Base.tables();
        sp = sp.substring(pattern.length(), sp.length() - 1);
        List tables = splitrim(sp, ",");
        for(Object t : c) {
          if(!tables.contains(t)){
          int count = count(s(t).trim());
            if(count != i(v)){
              throw new FyxtureVerifyFail(fmt("Table count for %s excepted <%d> but was <%d>", s(t), i(v), count));
            }
          }
        }
      }

      pattern = "count(";
      index = sp.indexOf(pattern);
      if(index > -1){
        sp = sp.substring(pattern.length(), sp.length() - 1);
        List tables = splitrim(sp, ",");
        for(Object t : tables) {
          assert count(s(t).trim()) == i(v);
        }
      }
    }
    return instance;
  }

  public static Fyxture load(String name) throws Throwable {
    init();
    logger.debug(name);
    Object o = Data.loader(name);
    if(o != null){
      if(o instanceof String){
        return load(s(o));
      }
      if(o instanceof List){
        for(Object t : list(o)){
          if(t instanceof Map) {
            for(Object k : m(t).keySet()){
              Object v = m(t).get(k);
              if(v instanceof String) {
                insert(s(k), s(v));
              }
              if(v instanceof List) {
                for(Object e : list(v)) {
                  insert(s(k), s(e));
                }
              }
            }
          }
          if(t instanceof String) {
            insert(s(t));
          }
          logger.info(t != null ? t.getClass() : null);
          logger.info(t != null ? t : null);
          
        }
      }
      if(o instanceof Map){
        for(Object t : m(o).keySet()){
          Object v = m(o).get(t);
          for(Object i : list(v)){
            insert(s(t), s(i));
          }
        }
      }
    }
    return instance;
  }

  void execute(String command) throws Throwable {
    logger.debug(command);
    statement.execute(command);
  }

  static ResultSet query(String command) throws Throwable {
    return statement.executeQuery(command);
  }

  private static List<String> alldbtables() throws Throwable {
    List excludes = list(Data.get("config", "common.verify.excludes"));
    String schema = s(Data.get("config", fmt("datasource.%s.schema", datasource)));
    List<String> c = new ArrayList<String>();
    ResultSet rs = connection.getMetaData().getTables(null, schema, "%", new String[] {"TABLE"});
    while(rs.next()){
      c.add(rs.getString(3));
    }
    return c;
  }

  private static String attributies(final String table) throws Throwable {
    
    logger.debug(table);

    String min = Data.ordinary();
    min = min == null ? "default" : min;

    StringBuffer minsb = new StringBuffer();
    minsb.append("#" + min + ":");
    minsb.append("\n##not nullable attributies");

    StringBuffer fullsb = new StringBuffer();
    fullsb.append("\n##nullable attributies");

    String schema = Data.schema(datasource);

    ResultSet rs = connection.getMetaData().getColumns(null, schema, table, "%");
    while(rs.next()){
      ResultSetMetaData rsmd = rs.getMetaData();
      if(rs.getString(11).equals("0")){
        minsb.append(cat("\n#   ", rs.getString(4), ":  \t\t\t\t#", rs.getString(6), "(", rs.getString(7), rs.getString(9) == null ? "" : ",", rs.getString(9) == null ? "" : rs.getString(9), ")"));
      }
      if(!rs.getString(11).equals("0")){
        fullsb.append(cat("\n#   ", rs.getString(4), ":  \t\t\t\t#", rs.getString(6), "(", rs.getString(7), rs.getString(9) == null ? "" : ",", rs.getString(9) == null ? "" : rs.getString(9), ")"));
      }
      logger.debug("" + rsmd.getColumnCount());
      for(int i = 1; i <= rsmd.getColumnCount(); i++){
        logger.debug(cat("" + i, rsmd.getColumnName(i), " : ", rs.getString(i)));
      }
    }
    fullsb.append("\n");
    minsb.append(fullsb.toString());
    return minsb.toString();
  }

  private Fyxture(String driver, String url, String user, String password, String dialect) throws Throwable {
    Class.forName(this.driver = driver).newInstance();
    this.statement = (this.connection = DriverManager.getConnection(this.url = url, this.user = user, this.password = password)).createStatement();
    dialect(dialect);
    auto();
  }

  private void auto() throws Throwable {
    String auto = Data.auto();
    List<String> tables = Base.tables();
    logger.debug(tables);
    if(auto != null){
      String suffix = Data.suffix();
      logger.debug(suffix);
      for(String table : tables){
        String filename = cat(datasource, "/", table, ".", suffix, ".yml");
        logger.debug(filename);
        try{
          Utils.class.getClassLoader().getResource(filename).getPath();
          logger.debug("   existe");
        }catch(Throwable t) {
          logger.debug("   não existe");
          File file = new File(cat(auto, "/", filename));
          logger.debug(file.getAbsolutePath());
          if(!file.exists()){
            if(!file.getParentFile().exists()){
              file.getParentFile().mkdirs();
            }
            file.createNewFile();
            FileUtils.writeStringToFile(file, attributies(table));
          }
        }
      }
    }
  }

  private void dialect(String descriptor) {
    logger.debug(descriptor);
    if(descriptor.equals("h2")){
      dialect = new H2Dialect(this);
      return;
    }
    if(descriptor.equals("oracle")){
      dialect = new OracleDialect(this);
      return;
    }
    if(descriptor.equals("sqlserver")){
      dialect = new SQLServerDialect(this);
      return;
    }
    throw new IllegalArgumentException("Incorrect Dialect Code");
  }

  private static Fyxture getInstance() throws Throwable {
    return new Fyxture(
      Data.driver(datasource),
      Data.url(datasource),
      Data.user(datasource),
      Data.password(datasource),
      Data.dialect(datasource)
    );
  }
}
