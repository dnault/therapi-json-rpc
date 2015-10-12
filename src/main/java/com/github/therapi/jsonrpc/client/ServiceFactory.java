package com.github.therapi.jsonrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodIntrospector;
import com.github.therapi.core.StandardMethodIntrospector;
import com.github.therapi.core.internal.MethodDefinition;
import com.github.therapi.jsonrpc.JsonRpcError;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.google.common.base.Throwables.propagate;

public class ServiceFactory {
    protected final MethodIntrospector methodIntrospector;
    protected final ObjectMapper objectMapper;

    public ServiceFactory(ObjectMapper objectMapper) {
        this(new StandardMethodIntrospector(objectMapper), objectMapper);
    }

    public ServiceFactory(MethodIntrospector methodIntrospector, ObjectMapper objectMapper) {
        this.methodIntrospector = methodIntrospector;
        this.objectMapper = objectMapper;
    }

    public <T> T createService(Class<T> serviceInterface) {
        InvocationHandler handler = new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                // todo wildly inefficient... should do this only *once*
                MethodDefinition methodDef = methodIntrospector.findMethods(proxy).stream()
                        .filter(mdef -> mdef.getMethod().equals(method)).findFirst().orElse(null);

                try {
                    ObjectNode request = createJsonRpcRequest(methodDef.getQualifiedName("."), args);

                    JsonRpcHttpClient client = new JdkHttpClient("http://localhost:8080/examples/jsonrpc");
                    JsonNode jsonRpcResponse = client.execute(objectMapper, request);

                    JsonNode result = jsonRpcResponse.get("result");

                    if (result == null) {
                        JsonNode error = jsonRpcResponse.get("error");
                        if (error == null) {
                            throw new IOException("invalid json-rpc response");
                        }

                        throw new JsonRpcException(objectMapper.convertValue(error, JsonRpcError.class));
                    }

                    return objectMapper.convertValue(result, methodDef.getReturnTypeRef());

                } catch (Exception e) {
                    throw propagate(e);
                }
            }
        };

        return serviceInterface.cast(Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class[]{serviceInterface}, handler));
    }

    protected ObjectNode createJsonRpcRequest(String method, Object... params) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("jsonrpc", "2.0")
                .put("id", "")
                .put("method", method);

        ArrayNode argsNode = objectMapper.createArrayNode();
        for (Object arg : params) {
            argsNode.addPOJO(arg);
        }
        request.set("params", argsNode);
        return request;
    }
}
