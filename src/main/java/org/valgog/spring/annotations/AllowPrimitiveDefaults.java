package org.valgog.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation should be used to allow using of the primitive defaults when a NULL value is read from the database by {@link AnnotatedRowMapper}
 * @author valgog
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AllowPrimitiveDefaults { }
