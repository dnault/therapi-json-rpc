package com.github.therapi.core.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.reflect.TypeToken;

public class JacksonHelper {

    public static TypeReference<?> getTypeReference(Parameter parameter, Class<?> contextForTypeVariableResolution) {
        return getResolvedTypeReference(parameter.getParameterizedType(), contextForTypeVariableResolution);
    }

    public static TypeReference<?> getReturnTypeReference(Method method, Class<?> contextForTypeVariableResolution) {
        return getResolvedTypeReference(method.getGenericReturnType(), contextForTypeVariableResolution);
    }

    public static TypeReference<?> getResolvedTypeReference(Type parameterizedType, Class<?> contextForTypeVariableResolution) {
        TypeToken token = TypeToken.of(contextForTypeVariableResolution);
        Type resolvedType = token.resolveType(parameterizedType).getType();
        return newTypeReference(resolvedType);
    }

    public static TypeReference<?> newTypeReference(Type type) {
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return type;
            }

            @Override
            public String toString() {
                return type.toString();
            }
        };
    }

    public static boolean isLikeNull(JsonNode node) {
        return node == null || node.isNull();
    }
}
