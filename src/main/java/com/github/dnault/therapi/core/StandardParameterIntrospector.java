package com.github.dnault.therapi.core;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.internal.ParameterDefinition;

import static com.github.dnault.therapi.core.internal.JacksonHelper.getTypeReference;

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

    private Supplier<?> getDefaultValueSupplier(Class<?> type) {
        if (type == Boolean.TYPE) {
            return () -> false;
        }
        if (type == Integer.TYPE) {
            return () -> 0;
        }
        if (type == Long.TYPE) {
            return () -> 0L;
        }
        if (type == Character.TYPE) {
            return () -> '\0';
        }
        if (type == Short.TYPE) {
            return () -> (short) 0;
        }
        if (type == Byte.TYPE) {
            return () -> (byte) 0;
        }
        if (type == Double.TYPE) {
            return () -> (double) 0;
        }
        if (type == Float.TYPE) {
            return () -> (float) 0;
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
