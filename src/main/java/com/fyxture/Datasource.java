package com.fyxture;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Datasource {
  private final String name;
  private final String dialect;
  private final String schema;
  private final String driver;
  private final String url;
  private final String user;
  private final String password;
  private final List<String> tables;
	private final Map sequences;
	private final List clearExcludes;

	public Datasource(String name) throws Throwable {
		this.name = name;
		this.dialect = Data.dialect(name);
		this.schema = Data.schema(name);
		this.driver = Data.driver(name);
		this.url = Data.url(name);
		this.user = Data.user(name);
		this.password = Data.password(name);
		this.tables = Base.tables();
		this.sequences  = Data.sequences(name);
		this.clearExcludes = Data.clear_excludes(name);
	}

	public Map sequences() {
		return sequences;
	}

	public Map sequences(String table) {
		Map result = Utils.m(sequences().get(table));
		if(result == null) {
			result = new LinkedHashMap();
		}
		return result;
  }

	public boolean excluded(String table) {
	  return clearExcludes.contains(table);
  }
}
