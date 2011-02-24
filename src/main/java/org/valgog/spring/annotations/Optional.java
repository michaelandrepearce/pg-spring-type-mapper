package org.valgog.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation should be used to mark the field as optional, when read from the database by {@link AnnotatedRowMapper}.
 * <p>So if the ResultSet does not contain this field, no exception will be thrown and the value will not be filled. 
 * <p>If the field, being marked with this annotation is a primitive type field, then it will be filled with 
 * default value for the corresponding primitive type.
 * @author valgog
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Optional { }
