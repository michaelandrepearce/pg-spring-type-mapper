package org.valgog.spring.example;

import java.util.Map;

import org.valgog.spring.annotations.DatabaseField;

public class SimpleWithMap {
	
	@DatabaseField
	private Map<String, String> simpleMap;

	public void setSimpleMap(Map<String, String> simpleMap) {
		this.simpleMap = simpleMap;
	}

	public Map<String, String> getSimpleMap() {
		return simpleMap;
	}
}
