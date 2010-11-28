package org.valgog.spring.tests.example;

import java.util.ArrayList;
import java.util.List;

import org.valgog.spring.annotations.DatabaseField;

public class ListWithEmbed {
	
	@DatabaseField(name="embeds")
	private List<WithEmbed> list = new ArrayList<WithEmbed>();

	public List<WithEmbed> getList() {
		return list;
	}

	public void setList(List<WithEmbed> list) {
		this.list = list;
	}

}
