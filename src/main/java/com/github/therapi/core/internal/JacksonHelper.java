package com.github.therapi.core.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class JacksonHelper {
    public static ObjectMapper newLenientObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule());

        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        return mapper;
    }

    public static TypeReference getTypeReference(Parameter parameter, Class<?> contextForTypeVariableResolution) {
        return getResolvedTypeReference(parameter.getParameterizedType(), contextForTypeVariableResolution);
    }

    public static TypeReference getReturnTypeReference(Method method, Class<?> contextForTypeVariableResolution) {
        return getResolvedTypeReference(method.getGenericReturnType(), contextForTypeVariableResolution);
    }

    public static TypeReference getResolvedTypeReference(Type parameterizedType, Class<?> contextForTypeVariableResolution) {
        TypeToken token = TypeToken.of(contextForTypeVariableResolution);
        Type resolvedType = token.resolveType(parameterizedType).getType();
        return newTypeReference(resolvedType);
    }

    public static TypeReference newTypeReference(Type type) {
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
