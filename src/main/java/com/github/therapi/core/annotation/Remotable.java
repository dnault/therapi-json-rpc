package com.github.therapi.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.therapi.runtimejavadoc.RetainJavadoc;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Inherited
@RetainJavadoc
public @interface Remotable {
    String DEFAULT_NAME = "_!$DEFAULT$!_";

    // The name of the remotable element. For classes, defaults to the simple class name.
    // For methods, defaults to the method name.
    String value() default DEFAULT_NAME;
}
