package com.github.therapi.core;

import static com.github.therapi.core.internal.JacksonHelper.getTypeReference;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.internal.TypesHelper;

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

    @Nullable
    protected Supplier<?> getDefaultValueSupplier(Parameter p, TypeReference typeReference) {
        Default defaultAnnotation = p.getAnnotation(Default.class);
        if (defaultAnnotation == null) {
            return null;
        }

        if (Default.NULL.equals(defaultAnnotation.value())) {
            return TypesHelper.getDefaultValueSupplier(p.getType());
        }

        String defaultValueStr = defaultAnnotation.value();

        if (p.getType().equals(String.class)) {
            return () -> defaultValueStr;
        }

        Supplier<?> result = () -> objectMapper.convertValue(defaultValueStr, typeReference);

        // fail fast; better to find out right away that the default value can't be deserialized
        result.get();

        return result;
    }

    protected String getName(Parameter p) {
        return p.getName();
    }

    protected boolean isNullable(Parameter p, Method method) {
        if (p.getAnnotation(Nullable.class) != null) {
            if (p.getType().isPrimitive()) {
                throw new InvalidAnnotationException("Annotation " + Nullable.class.getName()
                        + " may not be applied to primitive " + p.getType()
                        + " parameter '" + p.getName() + "' of method: " + method);
            }

            return true;
        }

        Default defaultAnnontation = p.getAnnotation(Default.class);
        return defaultAnnontation != null && Default.NULL.equals(defaultAnnontation.value());
    }
}
