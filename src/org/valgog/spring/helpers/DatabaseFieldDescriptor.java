package org.valgog.spring.helpers;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Set;

import org.valgog.spring.annotations.DataType;
import org.valgog.spring.helpers.exceptions.FieldDescriptionException;

/**
 * Interval class to hold information about mapping of some database column 
 * to a class field, used in {@link mappingDescriptorCache} cache.
 *
 */
public class DatabaseFieldDescriptor<C, T> extends ClassFieldDescriptor<C, T> {

	final private DataType databaseFieldType;
	final private String databaseFieldName;
	final private int databaseFieldIndex;
	final private EnumSet<MappingOption> options;
	
	public DatabaseFieldDescriptor(Field classField, DataType databaseFieldType, String databaseFieldName, int databaseFieldIndex, Set<MappingOption> options) throws FieldDescriptionException {
		super(classField);
		this.databaseFieldType = databaseFieldType;
		this.databaseFieldName = databaseFieldName;
		this.databaseFieldIndex = databaseFieldIndex;
		this.options = EnumSet.copyOf(options);
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