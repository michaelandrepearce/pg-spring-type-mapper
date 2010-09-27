package org.valgog.spring.tests.example;

public class SimpleObject {
	
	private int id;
	private String name;
	private String countryCode;
	private int[] lastMarks;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public int[] getLastMarks() {
		return lastMarks;
	}
	public void setLastMarks(int[] lastMarks) {
		this.lastMarks = lastMarks;
	}

}
