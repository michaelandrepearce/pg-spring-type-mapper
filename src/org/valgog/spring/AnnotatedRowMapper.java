package org.valgog.spring;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.valgog.spring.annotations.AllowPrimitiveDefaults;
import org.valgog.spring.annotations.DataType;
import org.valgog.spring.annotations.DatabaseField;

/**
 * This abstract class defines a row mapper to be able to map hierarchy of fields from the AbstractResultItem subclasses.
 * <p>A constructor should be overridden that takes a needed class type as parameter so a class instance can be created.
 * <p>method {@link #fillItem(ResultSet rs, ITEM item)} should be overridden to fill the fields of the passed item object. 
 * This object is already instantiated and should not be initialized specially.
 * 
 * <p>For examples see {@link BasePlaylistRowMapper} and {@link ExtendedPlaylistRowMapper}
 * 
 * @see BasePlaylist.Mapper
 * @see ExtendedPlaylist.Mapper
 * @see PlaylistSearchResultItem.Mapper
 *  
 * @author Valentine Gogichashvili
 *
 */
public class AnnotatedRowMapper<ITEM> 
	implements ParameterizedRowMapper<ITEM> {
	
	static final Logger logger = Logger.getLogger(AnnotatedRowMapper.class.getName());
	
	private Class<ITEM> itemType;

	private AnnotatedRowMapper( Class<ITEM> itemType ) {
		this.itemType = itemType; 
	}
	
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
	 * Should be overridden in case to fill {@code item} should be filled by non standard way.
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
	private static final Lock mappingDescriptorCacheReadLock = mappingDescriptorCacheLock.readLock();
	private static final Lock mappingDescriptorCacheWriteLock = mappingDescriptorCacheLock.writeLock();
	
	private static final Map<Class<?>, List<MappingDescriptor>> mappingDescriptorCache = new HashMap<Class<?>, List<MappingDescriptor>>();
	
	private static final class MappingDescriptor {

		private Field classField;

		private Method classFieldSetter;
		private DataType databaseFieldType;
		private String databaseFieldName;
		private boolean allowPrimitiveDefaults;
		
		public MappingDescriptor(Field classField, Method classFieldSetter, DataType databaseFieldType, String databaseFieldName, boolean allowPrimitiveDefaults) {
			this.classField = classField;
			this.classFieldSetter = classFieldSetter;
			this.databaseFieldType = databaseFieldType;
			this.databaseFieldName = databaseFieldName;
			this.allowPrimitiveDefaults = allowPrimitiveDefaults;
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
		public boolean isAllowPrimitiveDefaults() {
			return allowPrimitiveDefaults;
		}
		
	}
	
	/**
	 * This method can be used to extract the values of the annotated fields. 
	 * Annotation should be done by the annotation {@link DatabaseField}.
	 * 
	 * Here is an example of the field declaration:
	 * <pre>
	 * {@code @}DatabaseField(name = "u_id"){@code @}AllowPrimitiveDefaults
	 *  private int id;
	 * {@code @}DatabaseField(name = "u_nickname", type = DataType.TEXT)
	 *  private String nickname;
	 * {@code @}DatabaseField(name = "u_thumbnail", type = DataType.PATH_FULL)
	 *  private String thumbnail;
	 * {@code @}DatabaseField(name = "u_count_videos", type = DataType.INT4)
	 *  private int videoCount;
	 * </pre> 
	 * 
	 * This method can be used in when overriding {@link #fillItem(ResultSet, Object)} method to automate it.
	 * 
	 * @param itemClass the Class that has to be examined for annotations
	 * @param rs a {@link ResultSet} containing data to be extracted
	 * @param item a source item, that has to be filled with the extracted data 
	 * @throws SQLException thrown when data retrieving error happens
	 * @see DatabaseField
	 * @see AllowPrimitiveDefaults
	 * @see DataType
	 */
	private static final <ItemTYPE> void extractAnnotatedFieldValues(Class<ItemTYPE> itemClass, ResultSet rs, ItemTYPE item) throws SQLException {
		if ( itemClass == null ) throw new NullPointerException("itemClass should be not null");
		if ( item == null ) throw new NullPointerException("item should be not null");
		if ( rs == null ) throw new NullPointerException("rs should be not null");

		mappingDescriptorCacheReadLock.lock();
		try {
			List<MappingDescriptor> descList = mappingDescriptorCache.get(itemClass);
			if ( descList == null ) {
				mappingDescriptorCacheReadLock.unlock();
				mappingDescriptorCacheWriteLock.lock();
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
					mappingDescriptorCacheReadLock.lock();
					mappingDescriptorCacheWriteLock.unlock();
				}
			}
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
							desc.isAllowPrimitiveDefaults());
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
		} finally {
			mappingDescriptorCacheReadLock.unlock();
		}
	}

	private static <ItemTYPE> void extractMappingDescriptorsForClass(Class<ItemTYPE> itemClass, List<MappingDescriptor> descList) {
		Method[] itemMethods = itemClass.getDeclaredMethods();
		for (Method setter : itemMethods) {
			if ( setter.isSynthetic() ) continue;
			// if ( ! setter.isAccessible() ) continue;
			if ( Modifier.isStatic(setter.getModifiers()) ) continue;
			String methodName = setter.getName();
			if ( ! methodName.startsWith("set") ) continue;
			if ( methodName.length() <= 3 ) continue;
			String fieldName = java.beans.Introspector.decapitalize( methodName.substring(3) );
			Field field;
			try {
				field = itemClass.getDeclaredField(fieldName);
			} catch (SecurityException e) {
				continue;
			} catch (NoSuchFieldException e) {
				continue;
			}

			DatabaseField annotation = field.getAnnotation(DatabaseField.class);
			if ( annotation == null ) continue;
			boolean allowPrimitiveDefaults = field.isAnnotationPresent(AllowPrimitiveDefaults.class);
			
			DataType databaseFieldType = annotation.type();
			String databaseFieldName = annotation.name();
			if ( databaseFieldName.length() == 0 ) {
				// generate name from the class field name
				databaseFieldName = rewriteJavaPropertyNameToLowercaseUnderscoreName(fieldName);
			}
			MappingDescriptor desc = new MappingDescriptor(
					field, 
					setter,
					databaseFieldType, 
					databaseFieldName, 
					allowPrimitiveDefaults);							
			if ( logger.isLoggable(Level.FINE) ) {
				logger.fine("Property " + fieldName + " will be filled with the value of the database field [" + String.valueOf( databaseFieldName ) + "] "); 
			}
			descList.add(desc);
		}
	}
	
	public final ITEM mapRow(ResultSet rs, int rowNum) throws SQLException {
		ITEM item = newItemInstance();
		fillItem(rs, item);
		return item;
	}
}
