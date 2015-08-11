package com.github.dnault.therapi.core;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.internal.ParameterDefinition;

public class StandardParameterIntrospector implements ParameterIntrospector {
    private final ObjectMapper objectMapper;

    public StandardParameterIntrospector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ParameterDefinition> findParameters(Method method) {
        List<ParameterDefinition> params = new ArrayList<>();
        for (Parameter p : method.getParameters()) {
            params.add(new ParameterDefinition(getName(p), isNullable(p), getDefaultValueSupplier(p), p.getType()));
        }
        return params;
    }

    protected
    @Nullable
    Supplier<?> getDefaultValueSupplier(Parameter p) {
        Default defaultAnnotation = p.getAnnotation(Default.class);
        if (defaultAnnotation == null || Default.NULL.equals(defaultAnnotation.value())) {
            return null;
        }

        String defaultValueStr = defaultAnnotation.value();

        if (p.getType().equals(String.class)) {
            return () -> defaultValueStr;
        }

        return () -> objectMapper.convertValue(defaultValueStr, p.getType());
    }

    protected String getName(Parameter p) {
        return p.getName();
    }

    protected boolean isNullable(Parameter p) {
        if (p.getAnnotation(Nullable.class) != null) {
            return true;
        }

        Default defaultAnnontation = p.getAnnotation(Default.class);
        return defaultAnnontation != null && Default.NULL.equals(defaultAnnontation.value());
    }
}
