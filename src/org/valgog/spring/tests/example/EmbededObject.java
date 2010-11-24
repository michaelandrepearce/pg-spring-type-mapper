package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DatabaseField;

public class EmbededObject {
	
	@DatabaseField(name="x", position=0)
	private int x;
	
	@DatabaseField(name="y", position=1)
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
