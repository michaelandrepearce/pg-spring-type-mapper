package org.valgog.spring.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.valgog.spring.helpers.exceptions.FieldDescriptionException;

public class ClassFieldDescriptor<C, T> extends TypeDescriptor<T> {
	
	final private Field classField;
	final private Method classFieldSetter;
	
	public ClassFieldDescriptor(Field sourceField) throws FieldDescriptionException {
		super(sourceField);
		this.classField = sourceField;
		// find the setter 
		final String fieldName = sourceField.getName();
		final Class<?> fieldType = sourceField.getType();
		final Class<?> declaringClass = sourceField.getDeclaringClass();
		Method setter = null;
		try {
			final String setterName = "set" + capitalize( fieldName );
			setter = declaringClass.getDeclaredMethod(setterName, fieldType );
		} catch (SecurityException securityException) {
			throw new FieldDescriptionException( "Setter for the field " + declaringClass.getName() + '.' + fieldName + " could not be extracted: " + securityException.getMessage() );
		} catch (NoSuchMethodException e) {
			if ( ! Modifier.isPublic( classField.getModifiers() ) ) {
				throw new FieldDescriptionException("Setter for non-public field " + declaringClass.getName() + '.' + fieldName + " could not be found");
			}
		}
		this.classFieldSetter = setter;
	}
	
	public void assignFieldValue(C objectInstance, T fieldValue) throws FieldDescriptionException {
		try {
			if ( this.classFieldSetter != null ) {
				this.classFieldSetter.invoke(objectInstance, fieldValue);
			} else {
				this.classField.set(objectInstance, fieldValue);
			}
		} catch (IllegalArgumentException e) {
			throw new FieldDescriptionException(e);
		} catch (IllegalAccessException e) {
			throw new FieldDescriptionException(e);
		} catch (InvocationTargetException e) {
			throw new FieldDescriptionException(e);
		}
	}

	private static final String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		if (Character.isUpperCase(name.charAt(0))){
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
		
	}
}