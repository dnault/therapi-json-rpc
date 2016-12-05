package com.github.therapi.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.therapi.runtimejavadoc.RetainJavadoc;

@Retention(RUNTIME)
@Target(TYPE)
@RetainJavadoc
public @interface Remotable {
    String value();
}
