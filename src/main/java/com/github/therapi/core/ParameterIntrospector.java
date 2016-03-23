package com.github.therapi.core;

import java.lang.reflect.Method;
import java.util.List;

public interface ParameterIntrospector {
    List<ParameterDefinition> findParameters(Method method, Object owner);
}
