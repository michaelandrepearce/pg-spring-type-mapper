/**
 * 
 */
package org.valgog.spring.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.valgog.spring.helpers.exceptions.TypeInstantiationException;

public class TypeDescriptor<T> {
	final private Class<T> type;
	final private Type genericType;
	final private Type[] actualGenericParameterTypes;
	
	@SuppressWarnings("unchecked")
	public TypeDescriptor(Field sourceField) {
		this.type = (Class<T>) sourceField.getType();
		this.genericType = sourceField.getGenericType();
		if( genericType instanceof ParameterizedType ) {
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			this.actualGenericParameterTypes = parameterizedType.getActualTypeArguments();
		} else {
			this.actualGenericParameterTypes = null;
		}
	}
	
	public TypeDescriptor(Class<T> type) {
		this.type = type;
		this.genericType = null;
		this.actualGenericParameterTypes = null;
	}

	public Class<T> getType() {
		return type;
	}

	public Type getGenericType() {
		return genericType;
	}

	public Type[] getActualGenericParameterTypes() {
		return actualGenericParameterTypes;
	}
	
	/**
	 * Create an instance of the object of the given type
	 * @return new instance of the object of the given type
	 * @throws TypeInstantiationException 
	 * is thrown if it is not possible to create an instance 
	 * of the expected type
	 */
	public T newInstance() throws TypeInstantiationException {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new TypeInstantiationException(type, e);
		}
	}
	
	/**
	 * Returns component type of the array (if type is array) or of a Collection (if type is a subclass of Collection), 
	 * otherwise it returns null
	 * @return type of component or null
	 */
	@SuppressWarnings("unchecked")
	public <C> Class<C> getComponentType() {
		if ( type.isArray() ) {
			return (Class<C>) type.getComponentType();
		} else {
			if ( Collection.class.isAssignableFrom(type) && actualGenericParameterTypes != null && actualGenericParameterTypes.length == 1 ) {
				return (Class<C>) actualGenericParameterTypes[0];
			} else {
				return null;
			}
		}
	}
}