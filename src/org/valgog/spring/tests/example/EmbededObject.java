package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DatabaseField;

public class EmbededObject {
	
	@DatabaseField(name="x")
	private int x;
	
	@DatabaseField(name="y")
	private int y;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
}
