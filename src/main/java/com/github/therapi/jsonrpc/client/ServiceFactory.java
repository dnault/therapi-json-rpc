package com.github.therapi.jsonrpc.client;

import static com.github.therapi.core.internal.JacksonHelper.getReturnTypeReference;
import static com.google.common.base.Preconditions.checkArgument;
import static com.github.therapi.core.internal.LangHelper.propagate;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodIntrospector;
import com.github.therapi.core.StandardMethodIntrospector;
import com.github.therapi.core.annotation.Remotable;
import com.github.therapi.jsonrpc.JsonRpcError;

public class ServiceFactory {
    protected final MethodIntrospector methodIntrospector;
    protected final ObjectMapper objectMapper;
    protected final JsonRpcHttpClient httpClient;
    protected boolean useNamedArguments;

    public ServiceFactory(ObjectMapper objectMapper, JsonRpcHttpClient httpClient) {
        this(new StandardMethodIntrospector(objectMapper), objectMapper, httpClient);
    }

    public ServiceFactory(MethodIntrospector methodIntrospector, ObjectMapper objectMapper, JsonRpcHttpClient httpClient) {
        this.methodIntrospector = methodIntrospector;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public void setUseNamedArguments(boolean useNamedArguments) {
        this.useNamedArguments = useNamedArguments;
    }

    public <T> T createService(final Class<T> serviceInterface) {
        final String namespace = getNamespace(serviceInterface);

        return serviceInterface.cast(Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class[]{serviceInterface}, (proxy, method, args) -> {
                    try {
                        String qualifiedName = namespace.isEmpty() ? method.getName() : namespace + "." + method.getName();
                        ObjectNode request = createJsonRpcRequest(method, qualifiedName, args);

                        JsonNode jsonRpcResponse = httpClient.execute(objectMapper, request);

                        JsonNode result = jsonRpcResponse.get("result");

                        if (result == null) {
                            JsonNode error = jsonRpcResponse.get("error");
                            if (error == null) {
                                throw new IOException("invalid json-rpc response");
                            }

                            throw new JsonRpcException(objectMapper.convertValue(error, JsonRpcError.class));
                        }

                        if (method.getReturnType() == Void.TYPE) {
                            return null;
                        }

                        return objectMapper.convertValue(result, getReturnTypeReference(method, serviceInterface));

                    } catch (Exception e) {
                        throw propagate(e);
                    }
                }));
    }

    protected <T> String getNamespace(Class<T> serviceInterface) {
        Remotable annotation = serviceInterface.getAnnotation(Remotable.class);
        checkArgument(annotation != null, "%s is not annotated with @%s", serviceInterface, Remotable.class.getSimpleName());
        return annotation.value();
    }

    protected List<String> getParameterNames(Method m) {
        return Arrays.stream(m.getParameters())
                .map(Parameter::getName)
                .collect(toList());
    }

    protected ObjectNode createJsonRpcRequest(Method method, String methodName, Object... params) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("jsonrpc", "2.0")
                .put("id", "")
                .put("method", methodName);

        if (params == null) {
            return request;
        }

        if (useNamedArguments) {
            ObjectNode argsNode = objectMapper.createObjectNode();
            int index = 0;
            for (String paramName : getParameterNames(method)) {
                argsNode.putPOJO(paramName, params[index++]);
            }
            request.set("params", argsNode);

        } else {
            ArrayNode argsNode = objectMapper.createArrayNode();
            for (Object arg : params) {
                argsNode.addPOJO(arg);
            }
            request.set("params", argsNode);
        }

        return request;
    }
}
