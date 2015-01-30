package com.fyxture;

import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
	private static Map<String, Datasource> datasources = new LinkedHashMap<String, Datasource>();

	public static Datasource ds(String name) throws Throwable {
		if(name == null) {
			return ds(Data.datasource());
		}else{
			if(!datasources.containsKey(name)){
				datasources.put(name, new Datasource(name));
			}
		}
		return datasources.get(name);
	}

	public static Datasource ds() throws Throwable {
		return ds(null);
	}
}
