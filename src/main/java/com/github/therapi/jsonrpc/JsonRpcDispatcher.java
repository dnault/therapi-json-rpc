package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.therapi.core.MethodRegistry;

import java.io.InputStream;
import java.util.Optional;

/**
 * Implements the guts of JSON-RPC. Configure new instances using the {@link #builder(MethodRegistry)} method.
 */
public interface JsonRpcDispatcher {

    static JsonRpcDispatcherBuilder builder(MethodRegistry methodRegistry) {
        return new JsonRpcDispatcherBuilder(methodRegistry);
    }

    Optional<JsonNode> invoke(InputStream jsonRpcRequest);

    Optional<JsonNode> invoke(String jsonRpcRequest);

    MethodRegistry getMethodRegistry();
}
