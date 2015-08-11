package com.github.dnault.bozbar.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dnault.bozbar.core.internal.MethodDefinition;
import com.github.dnault.bozbar.core.internal.ParameterDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

public class MethodRegistry {
    private final HashMap<String, MethodDefinition> methodsByName = new HashMap<>();

    private MethodIntrospector scanner;
    private final ObjectMapper objectMapper;

    public MethodRegistry() {
        this(new ObjectMapper());
    }

    public MethodRegistry(ObjectMapper objectMapper) {
        this.objectMapper = requireNonNull(objectMapper);
        this.scanner = new StandardMethodIntrospector(objectMapper);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void scan(Object o) {
        for (MethodDefinition method : scanner.findMethods(o)) {
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

        System.out.println(method.getParameters());

        Object[] boundArgs = bindArgs(method, args);//new String[] {"Frank"};
        try {
            return objectMapper.convertValue(method.getMethod().invoke(method.getOwner(), boundArgs), JsonNode.class);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw propagate(e);
        }
    }

    private Object[] bindArgs(MethodDefinition method, JsonNode args) {
        if (args.isArray()) {
            return bindPositionalArguments(method, (ArrayNode) args);
        }

        return bindNamedArguments(method, (ObjectNode) args);


    }

    private Object[] bindNamedArguments(MethodDefinition method, ObjectNode args) {
        Object[] boundArgs = new Object[method.getParameters().size()];
        List<ParameterDefinition> params = method.getParameters();

        int i = 0;
        for (ParameterDefinition p : params) {
            Object arg = args.get(p.getName());

            if (arg == null && !p.isNullable()) {
                throw new ParameterBindingException("method '" + getName(method) + "' missing parameter '" + p.getName() + "' of type " + p.getType());
            }

            if (!args.has(p.getName()) && p.getDefaultValueSupplier().isPresent()) {
                boundArgs[i++] = p.getDefaultValueSupplier().get().get();
            } else {
                boundArgs[i++] = objectMapper.convertValue(arg, p.getType());
            }
        }

        return boundArgs;
    }

    private String getName(MethodDefinition method) {
        // todo use qualified name
        return method.getUnqualifiedName();
    }

    private Object[] bindPositionalArguments(MethodDefinition method, ArrayNode args) {
        Object[] boundArgs = new Object[method.getParameters().size()];
        List<ParameterDefinition> params = method.getParameters();

        if (args.size() > params.size()) {
            throw new ParameterBindingException("method '" + getName(method) + "' only accepts '" + params.size() + "' arguments but " + args.size() + " were provided");
        }

        for (int i = 0; i < params.size(); i++) {
            // check nullable / default value for args, extra args
            boundArgs[i] = objectMapper.convertValue(args.get(i), params.get(i).getType());
        }
        return boundArgs;
    }
}
