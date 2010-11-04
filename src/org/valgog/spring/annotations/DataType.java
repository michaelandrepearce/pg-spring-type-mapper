package org.valgog.spring.annotations;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This enumeration is used in the {@link DatabaseField} annotation 
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
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			Object value = rs.getObject( fieldIndex );
			if ( value instanceof Array ) {
				Array a = (Array) value;
				return a.getArray();
			}
			return value;
		}
	},

	/**
	 * This type can be used for short repeating texts so that the extracted Strings are pointing to the same cached Object 
	 * as defined in {@link String#intern()}.
	 */
	COMMON_TEXT {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			String s = rs.getString( fieldIndex );
			return ( s == null ) ? null : s.intern();
		}
	},
	
	/*
	 * Predefined data types with known conversion functions 
	 * (actually the automatic mapper should be working for all that primitive types,
	 * but they are added here as an example)
	 */
	
	INT2 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getShort(fieldIndex);
		}
	},
	
	INT4 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getInt(fieldIndex);
		}
	},

	INT8 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getLong(fieldIndex);
		}
	},
	
	TEXT {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getString( fieldIndex );
		}
	},
	
	REAL {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getDouble( fieldIndex );
		}
	},

	TIMESTAMP {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getTimestamp( fieldIndex );
		}
	},

	DATE {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getDate( fieldIndex );
		}
	},
	
	BOOLEAN {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			return rs.getBoolean( fieldIndex );
		}
	},

	SQL_ARRAY_INT4 {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			Array integerSQLArray = rs.getArray(fieldIndex);
			if ( integerSQLArray == null ) {
				return null;
			} else {
				return (Integer[]) integerSQLArray.getArray();
			}
		}
	},
	
	SQL_ARRAY_TEXT {
		@Override
		public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException {
			Array integerSQLArray = rs.getArray(fieldIndex);
			if ( integerSQLArray == null ) {
				return null;
			} else {
				return (String[]) integerSQLArray.getArray();
			}
		}
	};

	/**
	 * This map holds default values for the primitive types (except Void.TYPE type)
	 */
	public static final Map<Class<?>, Object> primitiveDefaults = Collections.unmodifiableMap( new HashMap<Class<?>, Object>() {

		private static final long serialVersionUID = -7481793517192394408L;

		// initialize the Map
		{
			put(Boolean.TYPE   , Boolean.FALSE );
			put(Character.TYPE , '\u0000' );
			put(Byte.TYPE      , (byte) 0 );
			put(Short.TYPE     , (short) 0 );
			put(Integer.TYPE   , 0 );
			put(Long.TYPE      , 0L );
			put(Float.TYPE     , 0.0f );
			put(Double.TYPE    , 0.0d );
		}
		
	} );
	
	
	abstract public Object extractFieldValueRaw(ResultSet rs, int fieldIndex) throws SQLException;
	
	public Object extractFieldValueRaw(ResultSet rs, String fieldName) throws SQLException {
		return extractFieldValueRaw(rs, rs.findColumn(fieldName));
	}

}
