package com.github.dnault.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.util.Optional;

public interface JsonRpcDispatcher {
    JsonNode invoke(InputStream jsonRpcRequest);

    JsonNode invoke(String jsonRpcRequest);
}
