package com.github.dnault.therapi.core;

import java.util.Collection;

import com.github.dnault.therapi.core.internal.MethodDefinition;

/**
 * Scans an object and returns a list of method definitions provided by the object.
 */
public interface MethodIntrospector {
    Collection<MethodDefinition> findMethods(Object o);
}
