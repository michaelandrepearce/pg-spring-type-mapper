package org.valgog.spring.example;

import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.DatabaseFieldNamePrefix;
import org.valgog.spring.annotations.Optional;

@DatabaseFieldNamePrefix("e_")
public class ExtendedClass extends SimpleClass {
	
	@DatabaseField
	private String fullName;
	
	@DatabaseField
	@Optional
	private int optionCount;

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setOptionCount(int optionCount) {
		this.optionCount = optionCount;
	}

	public int getOptionCount() {
		return optionCount;
	}
}
