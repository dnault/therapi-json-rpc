package com.github.dnault.bozbar.core;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dnault.bozbar.core.internal.MethodDefinition;

public class MethodRegistry {
    private final HashMap<String, MethodDefinition> methodsByName = new HashMap<>();

    private Introspector scanner = new StandardIntrospector();
    private final ObjectMapper objectMapper;

    public MethodRegistry() {
        this(new ObjectMapper());
    }

    public MethodRegistry(ObjectMapper objectMapper) {
        this.objectMapper = requireNonNull(objectMapper);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void scan(Object o) {
        for (MethodDefinition method : scanner.scan(o)) {
            methodsByName.put(method.getUnqualifiedName(), method);
        }
    }

    public JsonNode invoke(String methodName, JsonNode args) throws NoSuchMethodException {
        if (!args.isArray() && !args.isObject()) {
            throw new IllegalArgumentException("arguments must be ARRAY or OBJECT but encountered " + args.getNodeType());
        }

        MethodDefinition method = methodsByName.get(methodName);
        if (method == null) {
            throw new NoSuchMethodException(methodName);
        }

        Object[] boundArgs = new String[] {"Frank"};
        try {
            return objectMapper.convertValue(method.getMethod().invoke(method.getOwner(), boundArgs), JsonNode.class);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw propagate(e);
        }
    }
}
