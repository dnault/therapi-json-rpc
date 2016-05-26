package com.github.therapi.jsonrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodIntrospector;
import com.github.therapi.core.StandardMethodIntrospector;
import com.github.therapi.core.annotation.Remotable;
import com.github.therapi.jsonrpc.JsonRpcError;

import java.io.IOException;
import java.lang.reflect.Proxy;

import static com.github.therapi.core.internal.JacksonHelper.getReturnTypeReference;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Throwables.propagate;

public class ServiceFactory {
    protected final MethodIntrospector methodIntrospector;
    protected final ObjectMapper objectMapper;
    protected final JsonRpcHttpClient httpClient;

    public ServiceFactory(ObjectMapper objectMapper, JsonRpcHttpClient httpClient) {
        this(new StandardMethodIntrospector(objectMapper), objectMapper, httpClient);
    }

    public ServiceFactory(MethodIntrospector methodIntrospector, ObjectMapper objectMapper, JsonRpcHttpClient httpClient) {
        this.methodIntrospector = methodIntrospector;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public <T> T createService(final Class<T> serviceInterface) {
        final String namespace = getNamespace(serviceInterface);

        return serviceInterface.cast(Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class[]{serviceInterface}, (proxy, method, args) -> {
                    try {
                        String qualifiedName = namespace.isEmpty() ? method.getName() : namespace + "." + method.getName();
                        ObjectNode request = createJsonRpcRequest(qualifiedName, args);

                        JsonNode jsonRpcResponse = httpClient.execute(objectMapper, request);

                        JsonNode result = jsonRpcResponse.get("result");

                        if (result == null) {
                            JsonNode error = jsonRpcResponse.get("error");
                            if (error == null) {
                                throw new IOException("invalid json-rpc response");
                            }

                            throw new JsonRpcException(objectMapper.convertValue(error, JsonRpcError.class));
                        }

                        return objectMapper.convertValue(result, getReturnTypeReference(method, serviceInterface));

                    } catch (Exception e) {
                        throw propagate(e);
                    }
                }));
    }

    protected <T> String getNamespace(Class<T> serviceInterface) {
        Remotable annotation = serviceInterface.getAnnotation(Remotable.class);
        checkArgument(annotation != null, serviceInterface + " is not annotated with @" + Remotable.class.getSimpleName());
        return annotation.value();
    }

    protected ObjectNode createJsonRpcRequest(String method, Object... params) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("jsonrpc", "2.0")
                .put("id", "")
                .put("method", method);

        if (params != null) {
            ArrayNode argsNode = objectMapper.createArrayNode();
            for (Object arg : params) {
                argsNode.addPOJO(arg);
            }
            request.set("params", argsNode);
        }
        return request;
    }
}
