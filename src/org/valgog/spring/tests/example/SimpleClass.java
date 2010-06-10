package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DataType;
import org.valgog.spring.annotations.DatabaseFieldName;

public class SimpleClass {
	@DatabaseFieldName(value = "id")
	private int id;

	@DatabaseFieldName(value = "name")
	private String name;

	@DatabaseFieldName(value = "country_code", type=DataType.COMMON_TEXT)
	private String countryCode;

	@DatabaseFieldName(value = "last_marks")
	private int[] lastMarks;

	@DatabaseFieldName(value = "tags")
	private String[] tags;

	/*
	 * Setters and getters
	 * 
	 * Setters should be defined and public for the fields being mapped
	 * 
	 */
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

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

}
