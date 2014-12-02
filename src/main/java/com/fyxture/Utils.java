package com.fyxture;

import java.util.Map;

import org.apache.log4j.Logger;

public class Utils {
  private static Logger logger = Logger.getLogger(Utils.class);

  public static String s(Object value) {
    return ((String)value);
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
}
