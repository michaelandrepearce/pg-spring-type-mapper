package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DatabaseField;

public class SimpleRowClass {
	
	@DatabaseField(name = "st")
	private SimpleObject simpleObject;
	
	/*
	 * Setters and getters
	 * 
	 * Setters should be defined and public for the fields being mapped
	 * 
	 */
	public void setSimpleObject(SimpleObject simpleObject) {
		this.simpleObject = simpleObject;
	}

	public SimpleObject getSimpleObject() {
		return simpleObject;
	}
}
