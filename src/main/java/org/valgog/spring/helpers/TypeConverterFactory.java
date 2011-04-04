package org.valgog.spring.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.valgog.spring.helpers.exceptions.NotConvertable;

public class TypeConverterFactory {

	public <D> TypeConverter<String, D> getFromStringTypeConverter(Class<D> destinationType) {
		return getTypeConverter(String.class, destinationType );
	}
	
	public <S, D> TypeConverter<S, D> getTypeConverter(Class<S> sourceType, Class<D> destinationType) {
		// lookup the converter in the simple converter registry
		return null;
	}

	// Helper classes
	static class NullConverter<D> implements TypeConverter<Object, D> {
		@Override
		public D convertFrom(Object sourceValue) throws NotConvertable {
			if ( sourceValue != null ) throw new NotConvertable("NullConverter can only accept null values");
			return null;
		}
	}
	
	/**
	 * Marker interface, to be used for primitive type converters for getting 
	 * default value to be used in case of passing null value input string
	 *
	 * @param <N>
	 */
	static interface NeedsDefaultForNullValue<N> {
		public N getDefaultForNullValue();
	}
	
	private static class BooleanConverter implements TypeConverter<String, Boolean>, NeedsDefaultForNullValue<Boolean> {
		@Override
		public Boolean convertFrom(String sourceValue) throws NotConvertable {
			// TODO implement more possibilities for boolean input values
			return Boolean.valueOf(sourceValue);
		}

		@Override
		public Boolean getDefaultForNullValue() {
			return false;
		}
	}
	private static TypeConverter<String, Boolean> BOOLEAN_CONVERTER = new BooleanConverter();
	
	private static class CharacterConverter implements TypeConverter<String, Character>, NeedsDefaultForNullValue<Character> {
		@Override
		public Character convertFrom(String sourceValue) throws NotConvertable {
			if ( sourceValue == null ) return null;
			if ( sourceValue.length() != 1 ) throw new NotConvertable("Source string for char should be 1 char long");
			return sourceValue.charAt(0);
		}

		@Override
		public Character getDefaultForNullValue() {
			return ' ';
		}
	}	
	private static TypeConverter<String, Character> CHAR_CONVERTER = new CharacterConverter();
	
	/**
	 * NumericConverter class holds static converters from String to Numeric types
	 */
	static abstract class NumericConverter<N extends Number> implements TypeConverter<String, N>, NeedsDefaultForNullValue<N> {
		@Override
		public N convertFrom(String sourceValue) throws NotConvertable {
			try {
				return valueOf(sourceValue);
			} catch (NumberFormatException e) {
				throw new NotConvertable(String.class, Number.class, e.getMessage(), e);
			}
		}
		abstract protected N valueOf(String sourceValue) throws NumberFormatException;
		abstract public N getDefaultForNullValue();
	}
	private static final NumericConverter<Byte> BYTE_CONVERTER = new NumericConverter<Byte>() {
		@Override
		protected Byte valueOf(String sourceValue) throws NumberFormatException {
			return Byte.valueOf(sourceValue);
		}
		@Override
		public Byte getDefaultForNullValue() {
			return (byte) 0;
		}
	};
	private static final NumericConverter<Short> SHORT_CONVERTER = new NumericConverter<Short>() {
		@Override
		protected Short valueOf(String sourceValue) throws NumberFormatException {
			return Short.valueOf(sourceValue);
		}
		@Override
		public Short getDefaultForNullValue() {
			return (short) 0;
		}
	};
	private static final NumericConverter<Integer> INTEGER_CONVERTER = new NumericConverter<Integer>() {
		@Override
		protected Integer valueOf(String sourceValue) throws NumberFormatException {
			return Integer.valueOf(sourceValue);
		}
		@Override
		public Integer getDefaultForNullValue() {
			return 0;
		}
	};
	private static final NumericConverter<Long> LONG_CONVERTER = new NumericConverter<Long>() {
		@Override
		protected Long valueOf(String sourceValue) throws NumberFormatException {
			return Long.valueOf(sourceValue);
		}
		@Override
		public Long getDefaultForNullValue() {
			return 0L;
		}
	};
	private static final NumericConverter<Float> FLOAT_CONVERTER = new NumericConverter<Float>() {
		@Override
		protected Float valueOf(String sourceValue) throws NumberFormatException {
			return Float.valueOf(sourceValue);
		}
		@Override
		public Float getDefaultForNullValue() {
			return (float) 0.0;
		}
	};
	private static final NumericConverter<Double> DOUBLE_CONVERTER = new NumericConverter<Double>() {
		@Override
		protected Double valueOf(String sourceValue) throws NumberFormatException {
			return Double.valueOf(sourceValue);
		}
		@Override
		public Double getDefaultForNullValue() {
			return (double) 0.0;
		}
	};
	// Type converter registry
	
	private static final Map<Class<?>, TypeConverter<String, ?>> prefilledFromStringConerterRegistry;
	
	static {
		// fill the prefilledFromStringConerterRegistry with easy from string converters
		Map<Class<?>, TypeConverter<String, ?>> r = new HashMap<Class<?>, TypeConverter<String, ?>>();
		r.put( Byte.class, BYTE_CONVERTER );
		r.put( byte.class, BYTE_CONVERTER );
		r.put( Short.class, SHORT_CONVERTER );
		r.put( short.class, SHORT_CONVERTER );
		r.put( Integer.class, INTEGER_CONVERTER );
		r.put( int.class, INTEGER_CONVERTER );
		r.put( Long.class, LONG_CONVERTER );
		r.put( long.class, LONG_CONVERTER );
		r.put( Float.class, FLOAT_CONVERTER );
		r.put( float.class, FLOAT_CONVERTER );
		r.put( Double.class, DOUBLE_CONVERTER );
		r.put( double.class, DOUBLE_CONVERTER );
		r.put( Boolean.class, BOOLEAN_CONVERTER );
		r.put( boolean.class, BOOLEAN_CONVERTER );
		r.put( Character.class, CHAR_CONVERTER );
		r.put( char.class, CHAR_CONVERTER );
		prefilledFromStringConerterRegistry = Collections.unmodifiableMap(r);
	}
	
}
