package org.valgog.utils.exceptions;

import java.sql.SQLException;

public class ComplexTypeException extends SQLException {
	
	private static final long serialVersionUID = 1L;

	public ComplexTypeException() {
		super();
	}

	public ComplexTypeException(String arg0, String arg1, int arg2,
			Throwable arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ComplexTypeException(String arg0, String arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public ComplexTypeException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
	}

	public ComplexTypeException(String arg0, String arg1) {
		super(arg0, arg1);
	}

	public ComplexTypeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ComplexTypeException(String arg0) {
		super(arg0);
	}

	public ComplexTypeException(Throwable arg0) {
		super(arg0);
	}



}
