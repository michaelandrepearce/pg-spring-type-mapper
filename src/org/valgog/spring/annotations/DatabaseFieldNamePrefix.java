package org.valgog.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation can be used to allow prefixing all automatically generated field names of the class by the {@link AnnotatedRowMapper}
 * @author valgog
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD}) 
public @interface DatabaseFieldNamePrefix {
	String value();
}
