package org.valgog.spring.example;

import org.valgog.spring.annotations.DatabaseField;

public class SimpleRowClass {
	
	@DatabaseField(name = "st")
	private SimpleClass simpleObject;
	
	/*
	 * Setters and getters
	 * 
	 * Setters should be defined and public for the fields being mapped
	 * 
	 */
	public void setSimpleObject(SimpleClass simpleObject) {
		this.simpleObject = simpleObject;
	}

	public SimpleClass getSimpleObject() {
		return simpleObject;
	}
}
