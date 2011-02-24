package org.valgog.spring.helpers.exceptions;

import java.sql.SQLException;

public class FieldDescriptionException extends SQLException {

	private static final long serialVersionUID = 4033561250858425990L;

	public FieldDescriptionException(Exception e) {
		super(e);
	}

	public FieldDescriptionException(String string) {
		super(string);
	}
}
