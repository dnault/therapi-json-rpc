package com.github.therapi.core;

import java.lang.reflect.Method;
import java.util.List;

import com.github.therapi.core.internal.ParameterDefinition;

public interface ParameterIntrospector {
    List<ParameterDefinition> findParameters(Method method, Object owner);
}
