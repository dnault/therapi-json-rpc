package com.github.dnault.bozbar.core;

import java.util.Collection;

import com.github.dnault.bozbar.core.internal.MethodDefinition;

public interface Introspector {
    Collection<MethodDefinition> scan(Object o);
}
