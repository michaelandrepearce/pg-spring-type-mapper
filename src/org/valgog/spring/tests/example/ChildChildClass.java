package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DatabaseField;

public class ChildChildClass {
	
	@DatabaseField(name = "id")
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
