package org.valgog.spring.tests.example;

import java.util.ArrayList;
import java.util.List;

import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.GenericParameters;

public class ParentClass {
	
	@DatabaseField(name="children")
	@GenericParameters(ChildClass.class)
	private List<ChildClass> children = new ArrayList<ChildClass>();

	public List<ChildClass> getChildren() {
		return children;
	}

	public void setChildren(List<ChildClass> children) {
		this.children = children;
	}

}
