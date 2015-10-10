package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.util.Optional;

public interface JsonRpcDispatcher {
    Optional<JsonNode> invoke(InputStream jsonRpcRequest);

    Optional<JsonNode> invoke(String jsonRpcRequest);
}
