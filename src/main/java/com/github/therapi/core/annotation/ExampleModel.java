package com.github.therapi.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When applied to a public static method of an object scanned by the MethodRegistry,
 * indicates the serialized form of method's return value
 * should be included in the API documentation as an example of the model type.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ExampleModel {
    /**
     * If specified, the example will be associated with this model class instead of the example method's return type.
     */
    Class value() default Void.class;
}
