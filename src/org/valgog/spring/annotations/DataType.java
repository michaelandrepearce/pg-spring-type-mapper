package org.valgog.spring.annotations;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This enumeration is used in the {@link DatabaseFieldName} annotation 
 * (that it is why it should be an enumeration and not a generic class, that would be more convenient in our case).
 * 
 * <p>An abstract method {@link #extractFieldValue(ResultSet, String)} should be overridden to define a method of extraction 
 * of the database data type being described with a DataType being implemented.</p>
 * @author valgog
 *
 */
public enum DataType {

	AUTOMATIC {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getObject( fieldName );
		}
	},
	
	/*
	 * Predefined data types with known conversion functions 
	 * (actually the automatic mapper should be working for all that primitive types,
	 * but they are added here as an example)
	 */
	
	INT2 {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getShort(fieldName);
		}
	},
	
	INT4 {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getInt(fieldName);
		}
	},

	INT8 {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getLong(fieldName);
		}
	},
	
	TEXT {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getString( fieldName );
		}
	},
	
	COMMON_TEXT {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			String s = rs.getString( fieldName );
			return ( s == null ) ? null : s.intern();
		}
	},
	REAL {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getDouble( fieldName );
		}
	},

	TIMESTAMP {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getTimestamp( fieldName );
		}
	},

	DATE {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getDate( fieldName );
		}
	},
	
	BOOLEAN {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			return rs.getBoolean( fieldName );
		}
	},

	SQL_ARRAY_INT4 {
		@Override
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
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
		public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException {
			Array integerSQLArray = rs.getArray(fieldName);
			if ( integerSQLArray == null ) {
				return null;
			} else {
				return (String[]) integerSQLArray.getArray();
			}
		}
	};
	
	
	abstract public Object extractFieldValue(ResultSet rs, String fieldName) throws SQLException;

}
