package com.github.therapi.jsonrpc;

import java.io.InputStream;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonRpcDispatcher {
    Optional<JsonNode> invoke(InputStream jsonRpcRequest);

    Optional<JsonNode> invoke(String jsonRpcRequest);
}
