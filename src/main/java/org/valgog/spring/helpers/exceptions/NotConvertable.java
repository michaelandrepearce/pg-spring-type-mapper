package org.valgog.spring.helpers.exceptions;

/**
 * Thrown when TypeConvertor cannot convert 
 * @author valgog
 *
 */
public class NotConvertable extends Exception {

	private static final long serialVersionUID = 6406135394588460608L;

	public <S, D> NotConvertable(Class<S> sourceClass, Class<D> destinationClass, String message) {
		super(message);
	}

	public <S, D> NotConvertable(Class<S> sourceClass, Class<D> destinationClass, String message, Throwable cause) {
		super(message, cause);
	}

	public NotConvertable(String message) {
		super(message);
	}

}
