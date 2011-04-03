package org.valgog.spring.helpers;

import org.valgog.spring.helpers.exceptions.NotConvertable;

public interface TypeConverter<SOURCE_TYPE, DESTINATION_TYPE> {
	/**
	 * Convert sourceValue of type SOURCE_TYPE into the destination type DESTINATION_TYPE
	 * @param sourceValue
	 * @return instance of a type DESTINATION_TYPE that represents the sourceValue
	 */
	public DESTINATION_TYPE convertFrom(SOURCE_TYPE sourceValue) throws NotConvertable;
}
