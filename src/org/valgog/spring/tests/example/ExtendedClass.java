package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.DatabaseFieldNamePrefix;

@DatabaseFieldNamePrefix("e_")
public class ExtendedClass extends SimpleClass {
	
	@DatabaseField
	private String fullName;

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}
}
