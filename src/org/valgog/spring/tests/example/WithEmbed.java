package org.valgog.spring.tests.example;

import org.valgog.spring.annotations.Embed;

public class WithEmbed {
	
	@Embed
	EmbededObject embed;

	public EmbededObject getEmbed() {
		return embed;
	}

	public void setEmbed(EmbededObject embed) {
		this.embed = embed;
	}

}
