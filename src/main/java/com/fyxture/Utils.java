package com.fyxture;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class Utils {
  private static Logger logger = Logger.getLogger(Utils.class);

  public static String s(Object value) {
    return value == null ? null : value.toString();
  }

  public static Integer i(Object value) {
    return ((Integer)value);
  }

  public static Long l(Object value) {
    return value == null ? null : new Long(value.toString());
  }

  public static Map m(Object value) {
    return ((Map)value);
  }

  public static List list(Object value) {
    return ((List)value);
  }

  public static String fmt(String value, Object... args) {
    logger.debug(value);
    logger.debug(args.length);
    String result = String.format(value, args);
    logger.debug(result);
    return result;
  }

  public static String cat(String initial, String... parts) {
    for(String part : parts){
      initial = initial.concat(part);
    }
    return initial;
  }

  public static String quote(Object value) {
    return "" + (value instanceof String ? literal(value) : value);
  }

  public static String literal(Object value) {
    String result;
  	String svalue = s(value);
    logger.debug(svalue);
    if(svalue.length() != 0){
      switch(svalue.charAt(0)){
        case '$':
          result = svalue.substring(1);
          break;
        case '\\':
          svalue = svalue.substring(1);
        default:
          String [] all = svalue.split("'");
          svalue = all.length > 1 ? (StringUtils.join(all, "''") + (svalue.charAt(svalue.length() - 1) == '\'' ? "''" : "")) : svalue;
          result = "'" + svalue + "'";
      }
    }else{
      result = "''";
    }
  	return result;
  }

  public static String comma(String value) {
    return value == null || value.equals("") ? "" : ", ";
  }

  public static List<String> splitrim(String value, String pattern) {
    String [] parts = value.split(pattern);
    List<String> result = new ArrayList<String>();
    for (String part : parts) {
      result.add(part.trim());
    }
    return result;
  }
}
