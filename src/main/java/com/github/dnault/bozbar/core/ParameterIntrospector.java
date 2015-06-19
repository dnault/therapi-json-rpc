package com.github.dnault.bozbar.core;

import java.lang.reflect.Method;
import java.util.List;

import com.github.dnault.bozbar.core.internal.ParameterDefinition;

public interface ParameterIntrospector {
    List<ParameterDefinition> findParameters(Method method);
}
