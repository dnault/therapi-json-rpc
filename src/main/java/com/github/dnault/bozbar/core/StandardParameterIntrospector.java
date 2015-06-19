package com.github.dnault.bozbar.core;

import java.lang.reflect.Method;
import java.util.List;

import com.github.dnault.bozbar.core.internal.ParameterDefinition;
import com.google.common.collect.ImmutableList;

public class StandardParameterIntrospector implements ParameterIntrospector {
    @Override
    public List<ParameterDefinition> findParameters(Method method) {
        return ImmutableList.of();
    }
}
