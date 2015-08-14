package com.github.dnault.therapi.core.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

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

    public static TypeReference getTypeReference(Parameter parameter) {
        Type parameterizedType = parameter.getParameterizedType();
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return parameterizedType;
            }
        };
    }
}
