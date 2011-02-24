package org.valgog.spring.helpers.exceptions;

import java.sql.SQLException;

public class TypeInstantiationException extends SQLException {

	private static final long serialVersionUID = 4033561250858425990L;

	public TypeInstantiationException(Class<?> problematicType, Exception e) {
		super("Could not create an instance of expected type " + problematicType.getSimpleName(), e);
	}

}
