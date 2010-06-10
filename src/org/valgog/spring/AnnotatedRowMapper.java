package org.valgog.spring;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.valgog.spring.annotations.DataType;
import org.valgog.spring.annotations.DatabaseFieldName;

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

	public AnnotatedRowMapper( Class<ITEM> itemType ) {
		this.itemType = itemType; 
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
	
	/**
	 * This method can be used to extract the values of the annotated fields. 
	 * Annotation should be done by the annotation {@link DatabaseFieldName}.
	 * 
	 * Here is an example of the field declaration:
	 * <pre>
	 * {@code @}DatabaseFieldName(value = "u_id")
	 *  private int id;
	 * {@code @}DatabaseFieldName(value = "u_nickname", type = DataType.TEXT)
	 *  private String nickname;
	 * {@code @}DatabaseFieldName(value = "u_thumbnail", type = DataType.PATH_FULL)
	 *  private String thumbnail;
	 * {@code @}DatabaseFieldName(value = "u_count_videos", type = DataType.INT4)
	 *  private int videoCount;
	 * </pre> 
	 * 
	 * This method can be used in when overriding {@link #fillItem(ResultSet, Object)} method to automate it.
	 * 
	 * @param itemClass the Class that has to be examined for annotations
	 * @param rs a {@link ResultSet} containing data to be extracted
	 * @param item a source item, that has to be filled with the extracted data 
	 * @throws SQLException thrown when data retrieving error happens
	 * @see DatabaseFieldName
	 * @see DataType
	 */
	public static final <ItemTYPE> void extractAnnotatedFieldValues(Class<ItemTYPE> itemClass, ResultSet rs, ItemTYPE item) throws SQLException {
		if ( itemClass == null ) throw new NullPointerException("itemClass should be not null");
		if ( item == null ) throw new NullPointerException("item should be not null");
		if ( rs == null ) throw new NullPointerException("rs should be not null");

		// we extract annotated properties from the item and set the values for them
		
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
			DatabaseFieldName annotation = field.getAnnotation(DatabaseFieldName.class);

			// we have a needed annotation
			DatabaseFieldName a = (DatabaseFieldName) annotation;
			try {
				DataType type = a.type();
				String databaseFieldName = a.value();
				if ( logger.isLoggable(Level.FINE) ) {
					logger.fine("Property " + fieldName + " will be filled with the value of the database field [" + String.valueOf( databaseFieldName ) + "] "); 
				}
				Object value = type.extractFieldValue(rs, databaseFieldName);
				setter.invoke(item, value);
			} catch (IllegalArgumentException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("Trying to pass value of type ").append(a.type()).append(" to the method ");
				sb.append(setter.getName()).append('(').append(setter.getParameterTypes()[0].getName()).append(')');
				throw new IllegalArgumentException(sb.toString());
			} catch (IllegalAccessException e) {
				logger.warning(e.getMessage());
			} catch (InvocationTargetException e) {
				Throwable t = e.getCause();
				if ( t instanceof SQLException ) throw (SQLException) t;
				throw new SQLException( e.getCause().getMessage() );
			}
		}
	}
	
	public final ITEM mapRow(ResultSet rs, int rowNum) throws SQLException {
		ITEM item = newItemInstance();
		fillItem(rs, item);
		return item;
	}
}
