package com.github.therapi.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates the annotated parameter is optional and may be omitted from the request.
 * <p>For compatibility with positional parameter binding, default parameters should appear
 * together at the end of a method's parameter list.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Default {
    String NULL = "_!$NULL$!_";

    String value() default NULL;
}
