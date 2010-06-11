package org.valgog.spring.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

/**
 * This enumeration is used in the {@link DatabaseFieldName} annotation 
 * (that it is why it should be an enumeration and not a generic class, that would be more convenient in our case).
 * 
 * <p>An abstract method {@link #extractFieldValueRaw(ResultSet, String)} should be overridden to define a method of extraction 
 * of the database data type being described with a DataType being implemented.</p>
 * @author valgog
 *
 */
public enum DataType {

	AUTOMATIC {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			Object value = rs.getObject( fieldName );
			if ( value instanceof Array ) {
				Array a = (Array) value;
				return a.getArray();
			}
			return value;
		}
	},
	
	/*
	 * Predefined data types with known conversion functions 
	 * (actually the automatic mapper should be working for all that primitive types,
	 * but they are added here as an example)
	 */
	
	INT2 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getShort(fieldName);
		}
	},
	
	INT4 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getInt(fieldName);
		}
	},

	INT8 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getLong(fieldName);
		}
	},
	
	TEXT {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getString( fieldName );
		}
	},
	
	COMMON_TEXT {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			String s = rs.getString( fieldName );
			return ( s == null ) ? null : s.intern();
		}
	},
	REAL {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getDouble( fieldName );
		}
	},

	TIMESTAMP {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getTimestamp( fieldName );
		}
	},

	DATE {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getDate( fieldName );
		}
	},
	
	BOOLEAN {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			return rs.getBoolean( fieldName );
		}
	},

	SQL_ARRAY_INT4 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			Array integerSQLArray = rs.getArray(fieldName);
			if ( integerSQLArray == null ) {
				return null;
			} else {
				return (Integer[]) integerSQLArray.getArray();
			}
		}
	},
	
	SQL_ARRAY_TEXT {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
			Array integerSQLArray = rs.getArray(fieldName);
			if ( integerSQLArray == null ) {
				return null;
			} else {
				return (String[]) integerSQLArray.getArray();
			}
		}
	};
	
	public <T> T extractFieldValue(ResultSet rs, String fieldName, Class<T> fieldType) throws SQLException {
		Object value = extractFieldValueRaw(rs, fieldName);
		return makeAssignable(fieldType, value);
	}
	
	/*
				// not an array, maybe it is a primitive?
				if ( expectedType.isPrimitive() ) {
					if ( value instanceof Number ) {
						Number n = (Number) value;
						if ( expectedType == Integer.TYPE ) {
							return (T) n.intValue();
							
							java.lang.reflect.Array.
							
						} else if ( expectedType == Long.TYPE ) {
							return expectedType.cast(n.longValue());
						} else if ( expectedType == Short.TYPE ) {
							return expectedType.cast(n.shortValue());
						} else if ( expectedType == Byte.TYPE ) {
							return expectedType.cast(n.byteValue());
						} else if ( expectedType == Double.TYPE ) {
							return expectedType.cast(n.doubleValue());
						} else if ( expectedType == Float.TYPE ) {
							return expectedType.cast(n.floatValue());
						}
					} else if ( value instanceof Boolean ) {
						Boolean b = (Boolean) value;
						if ( expectedType == Boolean.TYPE ) return expectedType.cast(b.booleanValue());
					} else if ( value instanceof Character ) {
						Character c = (Character) value;
						if ( expectedType == Character.TYPE ) return expectedType.cast(c.charValue());
					} else if ( value instanceof CharSequence ) {
						// in case if we not a String and we expect a char, we transfer a first character only
						CharSequence cs = (CharSequence) value;
						if ( cs.length() > 0 ) return expectedType.cast(cs.charAt(0));
					}
				}

	 */
	
	
	@SuppressWarnings("unchecked")
	private static final <T> T makeAssignable(Class<T> expectedType, Object value) throws SQLException {
		if ( value == null ) { 
			if ( expectedType.isPrimitive() ) {
				// primitive types cannot be null, we have to rewrite the value to it's default?
				return null;
			} else {
				return null;
			}
		} else if ( expectedType.isInstance(value)) {
			// in normal case we should always get here (so the call to that method should not be very expensive
			return (T) value;
		} else {
			// object is not compatible with the fieldType, will try to do something with that
			if ( expectedType.isArray() && value.getClass().isArray() ) {
				// we expect an array type here, we have to rewrite the array in case an
				Object[] originalArray = (Object[]) value;
				Object newArray = java.lang.reflect.Array.newInstance(expectedType.getComponentType(), originalArray.length );
				for (int i = 0; i < originalArray.length; i++) {
					Object element = originalArray[i];
					try {
						java.lang.reflect.Array.set(newArray, i, makeAssignable(expectedType.getComponentType(), element) );
					} catch (IllegalArgumentException e) {
						// we have a NULL value, that is being assigned to the primitive array element. That is not possible
						// skipping this assignment will leave an element with a default value
					}
				}
				return expectedType.cast(newArray);
			} else {
				
				if ( expectedType.isPrimitive() ) {
					if ( value instanceof Number ) {
						return (T) value;
					} else if ( value instanceof Boolean ) {
						if ( expectedType == Boolean.TYPE ) return (T) value;;
					} else if ( value instanceof Character ) {
						if ( expectedType == Character.TYPE ) return (T) value;;
					} else if ( value instanceof CharSequence ) {
						// in case if we not a String and we expect a char, we transfer a first character only
						CharSequence cs = (CharSequence) value;
						Object c = cs.charAt(0);
						if ( cs.length() > 0 ) return (T) c;
					}
				}
				
				// now try to find a constructor, that will accept the given value (for example Integer(int) )
				try {
					Constructor<T> expectedTypeConstructor = expectedType.getDeclaredConstructor(value.getClass());
					return expectedTypeConstructor.newInstance(value);
				} catch (Exception ignore) {
					// Ok, the trick with the constructor did not work out, try the last String trick
					if ( expectedType.isAssignableFrom(CharSequence.class) ) {
						// expected type is a String compatible, in this case we just convert our value into the string and pass it so
						return expectedType.cast(value.toString());
					} 
				} 
			}
			throw new SQLException("Can not map recieved object of type " + value.getClass().getCanonicalName() + " to expected type " + expectedType.getCanonicalName() );
		}
	}
	
	abstract protected Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException;

}
