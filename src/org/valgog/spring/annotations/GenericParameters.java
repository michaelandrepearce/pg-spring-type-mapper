package org.valgog.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation can be used to mark field of an object to be visible by the {@link AnnotatedRowMapper}
 * @author valgog
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenericParameters {
	Class<?>[] value();
}
