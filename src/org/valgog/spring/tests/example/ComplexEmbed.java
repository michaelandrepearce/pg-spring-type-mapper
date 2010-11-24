package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.DatabaseField;

public class ComplexEmbed {
	
	@DatabaseField(name="x")
	private int x;
	
	@DatabaseField(name="embed")
	private WithEmbed withEmbed;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public WithEmbed getWithEmbed() {
		return withEmbed;
	}

	public void setWithEmbed(WithEmbed withEmbed) {
		this.withEmbed = withEmbed;
	}
	

}
