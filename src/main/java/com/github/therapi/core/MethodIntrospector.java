package com.github.therapi.core;

import java.util.Collection;

/**
 * Scans an object and returns a list of method definitions provided by the object.
 */
public interface MethodIntrospector {
    Collection<MethodDefinition> findMethods(Object o);

    String getNamespace(Class<?> serviceClass);
}
