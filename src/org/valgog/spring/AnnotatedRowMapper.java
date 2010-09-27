package org.valgog.spring;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.valgog.spring.annotations.AllowPrimitiveDefaults;
import org.valgog.spring.annotations.DataType;
import org.valgog.spring.annotations.DatabaseField;
import org.valgog.spring.annotations.DatabaseFieldNamePrefix;
import org.valgog.spring.annotations.Optional;

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
		List<MappingDescriptor> descList = getFieldDescriptorCache(itemClass);
		
		// use the cache
		for( MappingDescriptor desc : descList ) {
			final Field classField = desc.getClassField();
			final Method classFieldSetter = desc.getClassFieldSetter();
			final DataType dataType = desc.getDatabaseFieldType();
			try {
				Object value = dataType.extractFieldValue(
						rs, 
						desc.getDatabaseFieldName(), 
						classField.getType(), 
						desc.is(MappingOption.ALLOW_PRIMITIVE_DEFAULTS));
				// we do not use classField.set() method here, to always call the setter of our class to ensure, that all the needed operations, 
				// that could have been done in that setter are also executed
				classFieldSetter.invoke(item, value);
				
			} catch (IllegalAccessException e) {
				logger.warning(e.getMessage());
			} catch (InvocationTargetException e) {
				Throwable t = e.getCause();
				if ( t instanceof SQLException ) throw (SQLException) t;
				throw new SQLException( e.getCause().getMessage() );
			}
		}

	}

	static final <ItemTYPE> List<MappingDescriptor> getFieldDescriptorCache(Class<ItemTYPE> itemClass) {
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
	static final <ItemTYPE> int extractMappingDescriptorsForClass(Class<ItemTYPE> itemClass, List<MappingDescriptor> descList) {
		
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
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0))){
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
		
	}
	
	public final ITEM mapRow(ResultSet rs, int rowNum) throws SQLException {
		ITEM item = newItemInstance();
		fillItem(rs, item);
		return item;
	}
}
