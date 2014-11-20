package br.gov.serpro.fix;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class Db {
  private static Logger logger = Logger.getLogger(Db.class);
  private static Map<String, Object> map = new LinkedHashMap<String, Object>();
  private static Db instance;
  private static Connection connection;
  private static String driver;
  private static String url;
  private static String user;
  private static String password;
  private Object entity = null;
  private String entityname = null;
private String entityfilter;

  private Db(String driver, String url, String user, String password) {
    this.driver = driver;
    this.url = url;
    this.user = user;
    this.password = password;
  }

  public static Db init() throws Throwable {
	//logger.info(m(get("init.cfg")));
    if(instance == null){
      instance = new Db(
        s(get("init.cfg","dev.driver")), 
        s(get("init.cfg","dev.url")),
        s(get("init.cfg","dev.user")),
        s(get("init.cfg","dev.password"))
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

  public Db entity(String name, String filter) throws Throwable {
	entityname = name;
	entityfilter = filter;
	entity = m(get(name.concat(".ent"))).get(filter);
	return instance;
  }

  public Db entity(String name) throws Throwable {
	return entity(name, "default");
  }

  public Db insert() throws Throwable {
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
