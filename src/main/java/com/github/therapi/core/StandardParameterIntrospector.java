package com.github.therapi.core;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.internal.ParameterDefinition;

import static com.github.therapi.core.internal.JacksonHelper.getTypeReference;

public class StandardParameterIntrospector implements ParameterIntrospector {
    private final ObjectMapper objectMapper;

    public StandardParameterIntrospector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ParameterDefinition> findParameters(Method method, Object owner) {
        List<ParameterDefinition> params = new ArrayList<>();
        for (Parameter p : method.getParameters()) {
            TypeReference typeReference = getTypeReference(p, owner.getClass());
            params.add(new ParameterDefinition(getName(p), isNullable(p, method), getDefaultValueSupplier(p, typeReference), typeReference));
        }
        return params;
    }

    protected
    @Nullable
    Supplier<?> getDefaultValueSupplier(Parameter p, TypeReference typeReference) {
        Default defaultAnnotation = p.getAnnotation(Default.class);
        if (defaultAnnotation == null) {
            return null;
        }

        if (Default.NULL.equals(defaultAnnotation.value())) {
            return getDefaultValueSupplier(p.getType());
        }

        String defaultValueStr = defaultAnnotation.value();

        if (p.getType().equals(String.class)) {
            return () -> defaultValueStr;
        }

        return () -> objectMapper.convertValue(defaultValueStr, typeReference);
    }

    protected static Supplier<?> getDefaultValueSupplier(Class<?> type) {
        if (type == boolean.class) {
            return () -> false;
        }
        if (type == int.class) {
            return () -> 0;
        }
        if (type == long.class) {
            return () -> 0L;
        }
        if (type == char.class) {
            return () -> '\0';
        }
        if (type == short.class) {
            return () -> (short) 0;
        }
        if (type == byte.class) {
            return () -> (byte) 0;
        }
        if (type == double.class) {
            return () -> 0d;
        }
        if (type == float.class) {
            return () -> 0f;
        }
        return () -> null;
    }

    protected String getName(Parameter p) {
        return p.getName();
    }

    protected boolean isNullable(Parameter p, Method method) {
        if (p.getAnnotation(Nullable.class) != null) {
            if (p.getType().isPrimitive()) {
                throw new InvalidAnnotationException("Annotation " + Nullable.class.getName() + " may not be applied to primitive " + p.getType() + " parameter '" + p.getName() + "' of method: " + method);
            }

            return true;
        }

        Default defaultAnnontation = p.getAnnotation(Default.class);
        return defaultAnnontation != null && Default.NULL.equals(defaultAnnontation.value());
    }
}
