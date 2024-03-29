package org.valgog.spring.example;

import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.Embed;

public class WithEmbed {
	
	@Embed
	EmbededObject embed;
	
	@DatabaseField(name="z")
	private int z;

	public EmbededObject getEmbed() {
		return embed;
	}

	public void setEmbed(EmbededObject embed) {
		this.embed = embed;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

}
