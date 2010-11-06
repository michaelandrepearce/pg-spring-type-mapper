package org.valgog.spring;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.valgog.spring.annotations.AllowPrimitiveDefaults;
import org.valgog.spring.annotations.DataType;
import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.DatabaseFieldNamePrefix;
import org.valgog.spring.annotations.Optional;
import org.valgog.utils.ArrayParserException;
import org.valgog.utils.PostgresUtils;
import org.valgog.utils.RowParserException;

/**
 * This class defines a database row mapper to be able to map hierarchy of classes with properties with defined setters, 
 * that are marked with the specific annotations. 
 * 
 * Here is an example of the field declarations for a class, that can be mapped using {@link AnnotatedRowMapper}:
 * <pre>
 * {@code @}DatabaseField(name = "id"){@code @}AllowPrimitiveDefaults
 *  private int id;
 * {@code @}DatabaseField(name = "login_name", type = DataType.TEXT)
 *  private String loginName;
 * {@code @}DatabaseField(name = "words")
 *  private String[] words;
 *  
 *  ...
 *  // setter declarations for these fields
 * </pre> 
 * 
 * @see DatabaseField
 * @see AllowPrimitiveDefaults
 *  
 * @author valgog
 *
 */
public class AnnotatedRowMapper<ITEM> 
	implements ParameterizedRowMapper<ITEM> {
	
	static final Logger logger = Logger.getLogger(AnnotatedRowMapper.class.getName());
	
	private Class<ITEM> itemType;

	private AnnotatedRowMapper( Class<ITEM> itemType ) {
		this.itemType = itemType; 
	}
	
	/**
	 * Factory method to get an instance of the {@link AnnotatedRowMapper}.
	 * <p>The class, being mapped should define no constructors or, if defines, define a public default constructor.
	 * @param <ItemTYPE> Class type of the item to be mapped
	 * @param itemClass Class of the item to be mapped
	 * @return a new instance of the {@link AnnotatedRowMapper}
	 */
	public static final <ItemTYPE> AnnotatedRowMapper<ItemTYPE> getMapperForClass(Class<ItemTYPE> itemClass) {
		return new AnnotatedRowMapper<ItemTYPE>(itemClass);
	}
	
	private ITEM newItemInstance() {
		try {
			return (ITEM) itemType.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Could not create a new instance of type " + itemType.getName(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not create a new instance of type " + itemType.getName(), e);
		}
	}
	
	/**
	 * Should be overridden in case {@code item} should be filled by non standard way.
	 * <p>This method gets already instantiated empty {@code item} object, that should be filled.
	 * @param rs the source ResultSet, do not use next() call on that ResultSet. Only read data.
	 * @param item the source object that should be filled by this method. It should not be created inside the method.
	 * @throws SQLException exception that can be thrown when operating with the {@code rs} ResultSet. Should not be cached.
	 * 
	 * @see ResultSet
	 */
	protected void fillItem(ResultSet rs, ITEM item) throws SQLException {
		extractAnnotatedFieldValues(this.itemType, rs, item);
	}
	
	
	private static final String rewriteJavaPropertyNameToLowercaseUnderscoreName(String javaPropertyName) {
		//TODO: to be tested better
		
		if ( javaPropertyName == null ) throw new NullPointerException();
		final int length = javaPropertyName.length();
		StringBuilder r = new StringBuilder( length * 2 );
		
		// myFieldName -> my_field_name
		// MyFileName -> my_field_name
		// MyFILEName -> my_file_name
		// I was too lazy to write a small automata here... so quick and dirty by now
		boolean wasUpper = false;
		for (int i = 0; i < length; i++) {
			char ch = javaPropertyName.charAt(i);
			
			if ( Character.isUpperCase(ch) ) {
				if ( i > 0 ) { 
					if ( ( ! wasUpper ) && ( ch != '_' ) ) {
						r.append('_');
					}
				}
				ch = Character.toLowerCase(ch); 
				wasUpper = true;
			} else {
				if ( wasUpper ) {
					int p = r.length() - 2;
					if ( p > 1 && r.charAt(p) != '_' ) {
						r.insert(p, '_');
					}
				}
				wasUpper = false;
			}
			r.append(ch);
		}
		return r.toString();
	}
	
	private static final ReadWriteLock mappingDescriptorCacheLock = new ReentrantReadWriteLock();
	private static final Lock cacheReadLock = mappingDescriptorCacheLock.readLock();
	private static final Lock cacheWriteLock = mappingDescriptorCacheLock.writeLock();
	
	private static final Map<Class<?>, List<MappingDescriptor>> mappingDescriptorCache = new HashMap<Class<?>, List<MappingDescriptor>>();
	
	/**
	 * Private class to hold information about mapping of some database column to a class field, used in {@link mappingDescriptorCache} cache.
	 *
	 */
	private static final class MappingDescriptor {

		final private Field classField;
		final private Method classFieldSetter;
		final private DataType databaseFieldType;
		final private String databaseFieldName;
		final private int databaseFieldIndex;
		final private EnumSet<MappingOption> options;
		
		public MappingDescriptor(Field classField, Method classFieldSetter, DataType databaseFieldType, String databaseFieldName, int databaseFieldIndex, Set<MappingOption> options) {
			this.classField = classField;
			this.classFieldSetter = classFieldSetter;
			this.databaseFieldType = databaseFieldType;
			this.databaseFieldName = databaseFieldName;
			this.databaseFieldIndex = databaseFieldIndex;
			this.options = EnumSet.copyOf(options);
		}
		
		public Field getClassField() {
			return classField;
		}
		public Method getClassFieldSetter() {
			return classFieldSetter;
		}
		public DataType getDatabaseFieldType(){
			return databaseFieldType;
		}
		public String getDatabaseFieldName() {
			return databaseFieldName;
		}
		public int getDatabaseFieldIndex() {
			return databaseFieldIndex;
		}
		public boolean is(MappingOption option) {
			return options.contains(option);
		}
		
	}
	
	private static enum MappingOption {
		OPTIONAL, ALLOW_PRIMITIVE_DEFAULTS;
	}

	/**
	 * This method can be used to extract the values of the annotated fields. 
	 * Annotation should be done by the annotation {@link DatabaseField}.
	 * 
	 * @param itemClass the Class that has to be examined for annotations
	 * @param rs a {@link ResultSet} containing data to be extracted
	 * @param item a source item, that has to be filled with the extracted data 
	 * @throws SQLException thrown when data retrieving error happens
	 * @see DatabaseField
	 * @see AllowPrimitiveDefaults
	 * @see DataType
	 */
	static final <ItemTYPE> void extractAnnotatedFieldValues(Class<ItemTYPE> itemClass, ResultSet rs, ItemTYPE item) throws SQLException {
		if ( itemClass == null ) throw new NullPointerException("itemClass should be not null");
		if ( item == null ) throw new NullPointerException("item should be not null");
		if ( rs == null ) throw new NullPointerException("rs should be not null");

		// get cached structure
		final List<MappingDescriptor> descList = getFieldMappingDescriptorList(itemClass);
		final Connection connection = rs.getStatement().getConnection();
		// use the cache
		for( MappingDescriptor desc : descList ) {
			final Field classField = desc.getClassField();
			final Class<?> expectedType = classField.getType();
			final Method classFieldSetter = desc.getClassFieldSetter();
			final DataType dataType = desc.getDatabaseFieldType();
			final String fieldName = desc.getDatabaseFieldName();
			try {
				int fieldIndex = -1;
				try {
					fieldIndex = rs.findColumn(fieldName);
				} catch( SQLException e) {
					if ( desc.is(MappingOption.OPTIONAL) ) {
						continue; // skip the optional field if not found in the result set
					} else {
						throw e;
					}
				}
				
				Object rawValue = dataType.extractFieldValueRaw(rs, fieldIndex);
				
				Object value = makeAssignable(connection, expectedType, rawValue, desc.is(MappingOption.ALLOW_PRIMITIVE_DEFAULTS));
				
				if ( classFieldSetter != null ) {
					classFieldSetter.invoke(item, value);
				} else {
					classField.set(item, value);
				}
				
				
			} catch (IllegalAccessException e) {
				logger.warning(e.getMessage());
			} catch (InvocationTargetException e) {
				Throwable t = e.getCause();
				if ( t instanceof SQLException ) throw (SQLException) t;
				throw new SQLException( e.getCause().getMessage() );
			}
		}

	}

	/**
	 * Get a list of filed mapping descriptors for the given class type
	 * @param <ItemTYPE> Type of the class, that is being introspected
	 * @param itemClass Class defining the type of the class, that is being introspected
	 * @return list of {@link MappingDescriptor} objects, defining the given class type
	 */
	static final private <ItemTYPE> List<MappingDescriptor> getFieldMappingDescriptorList(Class<ItemTYPE> itemClass) {
		cacheReadLock.lock();
		try {
			List<MappingDescriptor> descList = mappingDescriptorCache.get(itemClass);
			if ( descList == null ) {
				cacheReadLock.unlock();
				cacheWriteLock.lock();
				try {
					// in case some fast competitor managed to fill the cache when we did relock, check once again
					descList = mappingDescriptorCache.get(itemClass);
					if ( descList == null ) { 
						// fill the cache
						descList = new ArrayList<MappingDescriptor>();
						extractMappingDescriptorsForClass(itemClass, descList);
						mappingDescriptorCache.put(itemClass, descList);
					}
				} finally {
					cacheReadLock.lock();
					cacheWriteLock.unlock();
				}
			}
			return descList;
		} finally {
			cacheReadLock.unlock();
		}
	}

	/**
	 * This method retrospects given {@code itemClass} and fills a list of {@link MappingDesriptor} objects, that define field mappings
	 * @param <ItemTYPE> source item class type
	 * @param itemClass source item class
	 * @param descList List of {@link MappingDesriptor} objects to filled with field mappings
	 */
	static final private <ItemTYPE> int extractMappingDescriptorsForClass(Class<ItemTYPE> itemClass, List<MappingDescriptor> descList) {
		
		if ( itemClass == null || Object.class.equals(itemClass) ) { 
			return 0;
		}
		
		// fill mapping descriptors for class super classes
		Class<? super ItemTYPE> itemSuperClass = itemClass.getSuperclass();
		final int superClassDatabaseFieldCount = extractMappingDescriptorsForClass(itemSuperClass, descList);
		
		// get global field name prefix if defined
		DatabaseFieldNamePrefix fieldNamePrefixAnnotation = itemClass.getAnnotation(DatabaseFieldNamePrefix.class);
		String globalPrefix = fieldNamePrefixAnnotation == null ? null : fieldNamePrefixAnnotation.value();
		int databaseFieldIndex = 0;
		Field[] itemFields = itemClass.getDeclaredFields();
		for (int i = 0 ; i < itemFields.length ; i++ ) {
			databaseFieldIndex = superClassDatabaseFieldCount + 1 + i;
			final Field field = itemFields[i];
			final String fieldName = field.getName();
			if ( field.isSynthetic() ) continue;
			// find the setter 
			Method setter;
			try {
				final String setterName = "set" + capitalize( fieldName );
				setter = itemClass.getDeclaredMethod(setterName, field.getType() );
			} catch (SecurityException securityException) {
				// skip this filed completely
				logger.warning("Skipping field " + fieldName + " for class " + itemClass.getName() + " as the setter could not be extracted: " + securityException.getMessage() );
				continue;
			} catch (NoSuchMethodException e) {
				setter = null;
			}
			EnumSet<MappingOption> mappingOptions = EnumSet.noneOf(MappingOption.class);
			DatabaseField annotation = field.getAnnotation(DatabaseField.class);
			if ( annotation == null ) continue;
			if ( field.isAnnotationPresent(AllowPrimitiveDefaults.class) ) {
				mappingOptions.add(MappingOption.ALLOW_PRIMITIVE_DEFAULTS);
			}
			if ( field.isAnnotationPresent(Optional.class) ) {
				mappingOptions.add(MappingOption.OPTIONAL);
			}
			
			DataType databaseFieldType = annotation.type();
			String databaseFieldName = annotation.name();
			if ( databaseFieldName == null || databaseFieldName.length() == 0 ) {
				// generate name from the class field name
				databaseFieldName = rewriteJavaPropertyNameToLowercaseUnderscoreName(fieldName);
				DatabaseFieldNamePrefix prefixAnnotation = field.getAnnotation(DatabaseFieldNamePrefix.class);
				if ( prefixAnnotation != null ) {
					String prefix = prefixAnnotation.value();
					if ( prefix != null && prefix.length() > 0 ) {
						databaseFieldName = prefix + databaseFieldName;
					}
				} else {
					// prefix annotation is not defined for that field, check if the global prefix annotation is defined
					if ( globalPrefix != null && globalPrefix.length() > 0 ) {
						databaseFieldName = globalPrefix + databaseFieldName;
					}
				}
			}
			MappingDescriptor desc = new MappingDescriptor(
					field, 
					setter,
					databaseFieldType, 
					databaseFieldName, 
					databaseFieldIndex,
					mappingOptions);
			if ( logger.isLoggable(Level.FINE) ) {
				logger.fine("Property " + fieldName + " will be filled with the value of the database field [" + String.valueOf( databaseFieldName ) + "] "); 
			}
			descList.add(desc);
		}
		return databaseFieldIndex;
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
	
	/**
	 * Make the given value assignable to the expected class type
	 * @param <T> expected class type
	 * @param expectedType expected class type
	 * @param value value to be converted
	 * @param allowPrimitiveDefaults if true, use default primitive values instead of null values
	 * @return assignable value of type <code>expectedType</code>
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	private static final <T> T makeAssignable(Connection connection, Class<T> expectedType, Object value, boolean allowPrimitiveDefaults) throws SQLException {
		if ( value == null ) { 
			if ( expectedType.isPrimitive() ) {
				// primitive types cannot be null, we have to rewrite the value to it's default?
				if ( allowPrimitiveDefaults ) {
					if ( logger.isLoggable(Level.FINE) ) logger.fine("Primitive type " + expectedType.getName() + " expected, rewriting NULL to primitive type default");
					return (T) DataType.primitiveDefaults.get(expectedType);
				} else {
					throw new SQLException("NULL value is not possible when filling a primitive type " + expectedType.getName() + ", if NULL values are needed, try to use not primitive wrapper classes as field types" );
				}
			} else {
				return null;
			}
		}
		
		if ( expectedType.isInstance(value) ) {
			// in normal case we should always get here (so the call to that method should not be very expensive)
			return (T) value;
		}

		if ( expectedType.isPrimitive() ) {
			// TODO: This part should be tested better
			if ( value instanceof Number ) {
				return (T) value;
			} else if ( value instanceof Boolean ) {
				if ( expectedType == Boolean.TYPE ) return (T) value;
			} else if ( value instanceof Character ) {
				if ( expectedType == Character.TYPE ) return (T) value;
			} else if ( value instanceof CharSequence ) {
				if ( expectedType == Character.TYPE ) {
					// in case if we get a String and we expect a char, we transfer a first character only
					CharSequence cs = (CharSequence) value;
					if ( cs.length() > 0 ) {
						Object c = cs.charAt(0);
						return (T) c;
					}
				}
			}
		}
				
		// object is not compatible with the fieldType, will try to do something about that
		// check if we are expecting an array
		if ( expectedType.isArray() && value.getClass().isArray() ) {
			// we expect an array type here, we have to rewrite the array in case an
			final Object[] originalArray = (Object[]) value;
			final Class<?> arrayComponentType = expectedType.getComponentType();
			Object newArray = java.lang.reflect.Array.newInstance(arrayComponentType, originalArray.length );
			for (int i = 0; i < originalArray.length; i++) {
				final Object element = originalArray[i];
				try {
					java.lang.reflect.Array.set(newArray, i, makeAssignable(connection, arrayComponentType, element, allowPrimitiveDefaults) );
				} catch (IllegalArgumentException e) {
					// we have a NULL value, that is being assigned to the primitive array element. That is not possible
					// skipping this assignment will leave an element with a default value.
					
					// actually, this should not happen now, as the value will be either substituted by the default value before 
					// or an exception will be already thrown
				}
			}
			return (T) newArray;
		}
		
		// try to map PGObject
		// this should be probably a ROW type, that we will try to map to some expected type
		// as field names are not available for abstract ROWs
		// we will try to map using field indexes
		if ( value instanceof PGobject ) {
			// get a string representation of the PGobject (is that really efficient?)
			String objectValue = ((PGobject)value).getValue();
			// TODO: should implement adaptor based processor for PGobject values
			try {
				T newObject = expectedType.newInstance();
				// split the received ROW string to array of string representations of the field components
				// and try to assign them to the expected type fields (using filed declaration index)
				List<MappingDescriptor> descList = getFieldMappingDescriptorList(expectedType);
				// optimization should be possible here, avoiding to create a temporary String List
				List<String> elementList = PostgresUtils.postgresROW2StringList(objectValue, 128);
				for (int i = 0, z = descList.size(); i < z; i++) {
					final MappingDescriptor desc = descList.get(i);
					final String elementValue;
					try {
						elementValue = elementList.get(i);
					} catch (IndexOutOfBoundsException e) {
						if (desc.is(MappingOption.OPTIONAL)) continue;
						throw e;
					}
					if (elementValue == null) continue; // skip nulls
					T element = makeAssignableFromString(expectedType, elementValue);
					Method setter = desc.getClassFieldSetter();
					if (setter == null) {
						desc.getClassField().set(newObject, element);
					} else {
						setter.invoke(newObject, element);
					}
				}
				System.out.println(elementList);
			} catch (RowParserException e) {
				throw new SQLException("Could not parse provided PGObject value: " + objectValue, e);
			} catch (InstantiationException e) {
				throw new SQLException("Could not instansiate object of type " + expectedType.getCanonicalName(), e);
			} catch (Exception e) {
				throw new SQLException("Could not convert PGObject value to type " + expectedType.getCanonicalName(), e);
			}
		}
		
		// now try to find a constructor, that will accept the given value (for example Integer(int) )
		try {
			Constructor<T> expectedTypeConstructor = expectedType.getDeclaredConstructor(value.getClass());
			return expectedTypeConstructor.newInstance(value);
		} catch (Exception ignore) {
			// Ok, the trick with the constructor did not work out, try the last String trick
			if ( expectedType.isAssignableFrom(CharSequence.class) ) {
				// expected type is String compatible, in this case we just convert our value into the string and pass it so
				return (T) value.toString();
			} 
		} 
		
		throw new SQLException( String.format( "Can not map recieved object of type %s to expected type %s", value.getClass().getCanonicalName(), expectedType.getCanonicalName()));
	}
	
	@SuppressWarnings("unchecked")
	private static final <T> T makeAssignableFromString(Class<T> expectedType, String value) throws SQLException {
		if ( value == null ) return null;
		
		if ( expectedType.isAssignableFrom(CharSequence.class) ) {
			return (T) value.toString();
		} else if ( expectedType.isAssignableFrom(Number.class) ) {
			try {
				Constructor<T> numberConstructor = expectedType.getDeclaredConstructor(String.class);
				return numberConstructor.newInstance(value);
			} catch (Exception e) {
				throw new SQLException( String.format("Could not parse given string [%s] as type %s", value, expectedType.getCanonicalName() ), e );
			}
		} else if ( expectedType.isPrimitive() ) {
			
		} else if ( expectedType.isArray() ) {
			Class<?> arrayComponentType = expectedType.getComponentType();
			// string should contain PostgreSQL array
			try {
				List<String> arrayElementValueList = PostgresUtils.postgresArray2StringList(value);
				final int arraySize = arrayElementValueList.size();
				T resultArray = (T) Array.newInstance(arrayComponentType, arraySize );
				for (int i = 0; i < arraySize; i++) {
					String elementValue = arrayElementValueList.get(i);
					if ( elementValue == null ) continue;
					Array.set(resultArray, i, makeAssignableFromString(arrayComponentType, elementValue));
				}
				return resultArray;
			} catch (ArrayParserException e) {
				throw new SQLException(e);
			}
		}
		
		// TODO: Construction Site
		return null;
	}
	
	
	public final ITEM mapRow(ResultSet rs, int rowNum) throws SQLException {
		ITEM item = newItemInstance();
		fillItem(rs, item);
		return item;
	}
}
