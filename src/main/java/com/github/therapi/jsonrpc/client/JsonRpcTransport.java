package com.github.therapi.jsonrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public interface JsonRpcTransport {
    JsonNode execute(ObjectMapper objectMapper, Object jsonRpcRequest) throws IOException;
}
