package org.valgog.spring.tests.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.valgog.spring.annotations.DatabaseField;

public class ParentClass {
	
	@DatabaseField(name="children")
	private List<ChildClass> children = new ArrayList<ChildClass>();
	@DatabaseField(name = "set")
	private Set<ChildClass> childrenSet = new HashSet<ChildClass>();
	
	public List<ChildClass> getChildren() {
		return children;
	}

	public void setChildren(List<ChildClass> children) {
		this.children = children;
	}

	public Set<ChildClass> getChildrenSet() {
		return childrenSet;
	}

	public void setChildrenSet(Set<ChildClass> childrenSet) {
		this.childrenSet = childrenSet;
	}

}
