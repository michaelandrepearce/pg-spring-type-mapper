package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DataType;
import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.AllowPrimitiveDefaults;

public class SimpleClass {
	
	@DatabaseField(name = "id")
	private int id;

	@DatabaseField(name = "name")
	private String name;

	@DatabaseField(name = "country_code", type=DataType.COMMON_TEXT)
	private String countryCode;

	@DatabaseField @AllowPrimitiveDefaults
	private int[] lastMarks;

	@DatabaseField(name = "tags")
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
