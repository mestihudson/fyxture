package com.fyxture;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.apache.commons.io.FileUtils;

import static com.fyxture.Utils.*;

public class Data {
  private static Logger logger = Logger.getLogger(Data.class);
  private static Map<String, Object> map = new LinkedHashMap<String, Object>();

  private static Object load(String filename) throws Throwable {
    if(map.get(filename) == null) {
      String filepath = Utils.class.getClassLoader().getResource(filename.concat(".yml")).getPath();
      logger.debug(filepath);
      InputStream input = new FileInputStream(new File(filepath));
      Yaml yaml = new Yaml();
      map.put(filename, yaml.load(input));
    }
    return map.get(filename);
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

  public static Object get(String filename, String... path) throws Throwable {
    Object o = load(filename);
    if(path.length != 0){
      o = path.length > 1 ? seg(o, path) : ges(o, path[0]);
    }
    return o;
  }

  public static String sequence(String table) throws Throwable {
    return sequence(table, "name");
  }

  public static String sequence(String table, String property) throws Throwable {
    return s(get("config", fmt("table.%s.sequence.%s", table, property)));
  }

  public static String datasource() throws Throwable {
    return s(get("config","common.datasource.default"));
  }

  public static Object verify(String name) throws Throwable {
    return get("config", fmt("verify.%s", name));
  }

  public static String ordinary() throws Throwable {
    return s(get("config", "common.table.default"));
  }

  public static Map tables() throws Throwable {
    return m(get("config", "table"));
  }

  public static Collection table_names() throws Throwable {
    return list(get("config", "common.table.clear"));
  }

  public static Object loader(String name) throws Throwable {
    return get("config", fmt("load.%s", name));
  }

  public static String schema(String datasource) throws Throwable {
    return s(get("config", fmt("datasource.%s.schema", datasource)));
  }

  public static List excludes() throws Throwable {
    return list(get("config", "common.verify.excludes"));
  }

  public static String auto() throws Throwable {
    return s(get("config", "common.table.auto"));
  }

  public static String suffix() throws Throwable {
    return s(get("config", "common.table.suffix"));
  }

  public static String driver(String datasource) throws Throwable {
    return s(get("config", fmt("datasource.%s.driver", datasource)));
  }

  public static String url(String datasource) throws Throwable {
    return s(get("config", fmt("datasource.%s.url", datasource)));
  }

  public static String user(String datasource) throws Throwable {
    return s(get("config", fmt("datasource.%s.user", datasource)));
  }

  public static String password(String datasource) throws Throwable {
    return s(get("config", fmt("datasource.%s.password", datasource)));
  }

  public static String dialect(String datasource) throws Throwable {
    return s(get("config", fmt("datasource.%s.dialect", datasource)));
  }

  public static Object instances(String datasource, String table, String suffix) throws Throwable {
    return Data.get(datasource + "/" + table + "." + suffix);
  }

  public static Object instance(String datasource, String table, String suffix, String descriptor) throws Throwable {
    return get(fmt("%s/%s.%s", datasource, table, suffix), descriptor);
  }
}
