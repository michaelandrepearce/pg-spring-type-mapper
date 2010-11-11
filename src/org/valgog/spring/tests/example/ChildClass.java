package org.valgog.spring.tests.example;

import java.util.List;

import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.GenericParameters;

public class ChildClass {
	
	@DatabaseField(name = "id", position=0)
	private Integer id;
	
	@DatabaseField(name = "child", position=1)
	private ChildChildClass child;
	
	@DatabaseField(name = "children", position=2)
	@GenericParameters(ChildChildClass.class)
	private List<ChildChildClass> children;	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ChildChildClass getChild() {
		return child;
	}

	public void setChild(ChildChildClass child) {
		this.child = child;
	}

	public List<ChildChildClass> getChildren() {
		return children;
	}

	public void setChildren(List<ChildChildClass> children) {
		this.children = children;
	}

}
