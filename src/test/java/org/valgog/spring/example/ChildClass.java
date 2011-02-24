package org.valgog.spring.example;

import java.util.List;

import org.valgog.spring.annotations.DatabaseField;

public class ChildClass {
	
	@DatabaseField(name = "id")
	private Integer id;
	
	@DatabaseField(name = "child")
	private ChildChildClass child;
	
	@DatabaseField(name = "children")
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
