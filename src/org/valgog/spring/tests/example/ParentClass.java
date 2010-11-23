package org.valgog.spring.tests.example;

import java.util.ArrayList;
import java.util.List;

import org.valgog.spring.annotations.DatabaseField;

public class ParentClass {
	
	@DatabaseField(name="children")
	private List<ChildClass> children = new ArrayList<ChildClass>();
	
	public List<ChildClass> getChildren() {
		return children;
	}

	public void setChildren(List<ChildClass> children) {
		this.children = children;
	}

}
