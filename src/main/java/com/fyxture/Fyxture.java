package com.fyxture;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.apache.commons.io.FileUtils;

import static com.fyxture.Utils.*;

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
  private static Dialect dialect;

  private static final String SELECT = "SELECT %s FROM %s WHERE %s";
  private static final String NL = "\n";

  public static void clear() throws Throwable {
    init();
    Map tables = m(get("config", "table"));
    Collection table_names = list(get("config", "common.table.clear"));
    if(table_names == null || table_names.isEmpty()){
      table_names = tables.keySet();
    }
    logger.debug(tables);
    logger.debug(table_names);
    for(Object table : table_names) {
      dialect.delete(s(table));
      if(tables.containsKey(table)){
        dialect.reset_sequence(s(table));
      }
    }
  }

  void execute(String command) throws Throwable {
    logger.debug(command);
    statement.execute(command);
  }

  public static Integer count(String table) throws Throwable {
    init();
    ResultSet rs = query(fmt(dialect.count_command(), table));
    rs.next();
    return rs.getInt(1);
  }

  public static Fyxture insert(String table) throws Throwable {    
    insert(table, s(get("config", "common.table.default")));
    return instance;
  }

  public static Fyxture insert(String table, String descriptor) throws Throwable {
    insert(table, descriptor, new Pair[]{});
    return instance;
  }

  public static Fyxture insert(String table, String descriptor, Pair... pairs) throws Throwable {
    init();

    Map<String, Object> decoded = new LinkedHashMap<String, Object>();
    for(Pair pair : pairs){
      decoded.put(pair.key, pair.value);
    }
    logger.debug(decoded);

    String suffix = s(get("config", "common.table.suffix"));
    Object o = get(fmt("%s/%s.%s", datasource, table, suffix), descriptor);
    Map c = m(o);
    logger.debug(c);

    logger.debug(m(get(datasource + "/" + table + ".table")));

    List<String> columns = new ArrayList<String>();
    List<Object> values = new ArrayList<Object>();
    for(Object key : c.keySet()){
      columns.add(s(key));
      values.add(decoded.get(key) == null ? (c.get(key) == null ? null : c.get(key)) : decoded.get(key));
    }
    dialect.insert(table, columns, values);
    return instance;
  }

  static ResultSet query(String command) throws Throwable {
    return statement.executeQuery(command);
  }

  String sequence(String table) throws Throwable {
    return sequence(table, "name");
  }

  String sequence(String table, String property) throws Throwable {
    return s(get("config", fmt("table.%s.sequence.%s", table, property)));
  }

  public static Fyxture insert(final String table, final Pair... pairs) throws Throwable {
    String descriptor = s(get("config", "common.table.default"));
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

  private Fyxture(String driver, String url, String user, String password, String dialect) throws Throwable {
    Class.forName(this.driver = driver).newInstance();
    this.statement = (this.connection = DriverManager.getConnection(this.url = url, this.user = user, this.password = password)).createStatement();
    dialect(dialect);
    auto();
  }

  private void auto() throws Throwable {
    String auto = s(get("config", "common.table.auto"));
    logger.debug(dbtables());
    if(auto != null){
      String suffix = s(get("config", "common.table.suffix"));
      logger.debug(suffix);
      for(String table : dbtables()){
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
            //logger.info(attributies(table));
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

  public static Fyxture init() throws Throwable {
    String ds = datasource == null ? s(get("config","common.datasource.default")) : datasource;
    logger.debug(ds);
    return init(ds);
  }

  public static Fyxture init(String datasourcename) throws Throwable {
    logger.debug(datasourcename);
    if(!datasourcename.equals(datasource)){
      datasource = datasourcename;
      instance = getInstance();
    }
    logger.debug(instance);
    return instance;
  }

  public static Fyxture verify(String name) throws Throwable {
    init();
    logger.debug(name);
    Object o = get("config", fmt("verify.%s", name));
    if(o instanceof String) {
      return verify(s(o));
    }
    for(Object p : m(o).keySet()){
      Object v = m(o).get(p);
      String sp = s(p);

      String pattern = "count~(";
      Integer index = sp.indexOf(pattern);
      if(index > -1){
        List c = dbtables();
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

  private static List<String> alldbtables() throws Throwable {
    List excludes = list(get("config", "common.verify.excludes"));
    String schema = s(get("config", fmt("datasource.%s.schema", datasource)));
    List<String> c = new ArrayList<String>();
    ResultSet rs = connection.getMetaData().getTables(null, schema, "%", new String[] {"TABLE"});
    while(rs.next()){
      c.add(rs.getString(3));
    }
    return c;
  }

  private static String attributies(final String table) throws Throwable {
    
    logger.debug(table);

    String min = s(get("config", "common.table.default"));
    min = min == null ? "default" : min;

    StringBuffer minsb = new StringBuffer();
    minsb.append("#" + min + ":");
    minsb.append("\n##not nullable attributies");

    StringBuffer fullsb = new StringBuffer();
    fullsb.append("\n##nullable attributies");

    String schema = s(get("config", fmt("datasource.%s.schema", datasource)));

    ResultSet rs = connection.getMetaData().getColumns(null, schema, table, "%");
    while(rs.next()){
      ResultSetMetaData rsmd = rs.getMetaData();
      if(rs.getString(11).equals("0")){
        minsb.append(cat("\n#   ", rs.getString(4), ":  \t\t\t\t#", rs.getString(6), "(", rs.getString(7), rs.getString(9) == null ? "" : ",", rs.getString(9) == null ? "" : rs.getString(9), ")"));
      }
      if(!rs.getString(11).equals("0")){
        fullsb.append(cat("\n#   ", rs.getString(4), ":  \t\t\t\t#", rs.getString(6), "(", rs.getString(7), rs.getString(9) == null ? "" : ",", rs.getString(9) == null ? "" : rs.getString(9), ")"));
      }
      System.out.println("" + rsmd.getColumnCount());
      for(int i = 1; i <= rsmd.getColumnCount(); i++){
        System.out.println(cat("" + i, rsmd.getColumnName(i), " : ", rs.getString(i)));
      }
    }
    fullsb.append("\n");
    minsb.append(fullsb.toString());
    return minsb.toString();
  }

  private static List<String> dbtables() throws Throwable {
    return dbtables("%");
  }

  private static List<String> dbtables(final String pattern) throws Throwable {
    List excludes = list(get("config", "common.verify.excludes"));
    String schema = s(get("config", fmt("datasource.%s.schema", datasource)));
    List<String> c = new ArrayList<String>();
    ResultSet rs = connection.getMetaData().getTables(null, schema, pattern, new String[] {"TABLE"});
    while(rs.next()){
      String table = rs.getString(3);
      if(!excludes.contains(table)){
        c.add(table);
      }
    }
    return c;
  }

  public static Fyxture load(String name) throws Throwable {
    init();
    logger.debug(name);
    Object o = get("config", fmt("load.%s", name));
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


  private static Fyxture getInstance() throws Throwable {
    return new Fyxture(
      s(get("config", fmt("datasource.%s.driver", datasource))),
      s(get("config", fmt("datasource.%s.url", datasource))),
      s(get("config", fmt("datasource.%s.user", datasource))),
      s(get("config", fmt("datasource.%s.password", datasource))),
      s(get("config", fmt("datasource.%s.dialect", datasource)))
    );
  }

  private static Object get(String filename, String... path) throws Throwable {
    Object o = cargo(filename);
    if(path.length != 0){
      o = path.length > 1 ? seg(o, path) : ges(o, path[0]);
    }
    return o;
  }

  private static Object ges(Object o, String path) {
    Object result = seg(o, path.split("\\."));
    return result;
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

  private static Object cargo(String filename) throws Throwable {
    if(map.get(filename) == null) {
      String filepath = Utils.class.getClassLoader().getResource(filename.concat(".yml")).getPath();
      logger.debug(filepath);
      InputStream input = new FileInputStream(new File(filepath));
      Yaml yaml = new Yaml();
      map.put(filename, yaml.load(input));
    }
    return map.get(filename);
  }
}
