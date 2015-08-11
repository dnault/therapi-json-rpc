package com.github.dnault.therapi.core;

import java.lang.reflect.Method;
import java.util.List;

import com.github.dnault.therapi.core.internal.ParameterDefinition;

public interface ParameterIntrospector {
    List<ParameterDefinition> findParameters(Method method);
}
