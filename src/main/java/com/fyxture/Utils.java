package com.fyxture;

import java.util.Map;

import org.apache.log4j.Logger;

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

  public static String fmt(String value, Object... args) {
    logger.debug(value);
    logger.debug(args.length);
    return String.format(value, args);
  }

  public static String cat(String initial, String... parts) {
    for(String part : parts){
      initial = initial.concat(part);
    }
    return initial;
  }

  public static String quote(Object value) {
    return "" + (value instanceof String ? "'" + value + "'" : value);
  }

  public static String comma(String value) throws Throwable {
    return value == null || value.equals("") ? "" : ", ";
  }
}
