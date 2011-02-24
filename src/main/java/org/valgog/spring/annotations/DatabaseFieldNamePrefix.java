package org.valgog.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation can be used by the {@link AnnotatedRowMapper} to allow prefixing all automatically generated field names of the annotated class 
 * or fields annotated with this annotation (when the name is not defined by the {@link DatabaseField) annotation.
 * @author valgog
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD}) 
public @interface DatabaseFieldNamePrefix {
	String value() default "";
}
