package com.github.therapi.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the arguments and/or return value of the annotated method should not be logged.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoNotLog {
  Scope value() default Scope.BOTH;

  enum Scope {
    /**
     * Indicates the request should not be logged, but it's okay to log the response.
     */
    REQUEST,

    /**
     * Indicates the response should not be logged, but it's okay to log the request.
     */
    RESPONSE,

    /**
     * Indicates neither the request nor the response should be logged.
     */
    BOTH
  }
}
