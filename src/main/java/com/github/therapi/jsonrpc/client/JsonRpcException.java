package com.github.therapi.jsonrpc.client;

import com.github.therapi.jsonrpc.JsonRpcError;

public class JsonRpcException extends RuntimeException {
    private final JsonRpcError jsonRpcError;

    public JsonRpcException(JsonRpcError jsonRpcError) {
        super(getExceptionMessage(jsonRpcError));
        this.jsonRpcError = jsonRpcError;
    }

    private static String getExceptionMessage(JsonRpcError jsonRpcError) {
        if (jsonRpcError.getData() == null || !jsonRpcError.getData().containsKey("detail")) {
            return jsonRpcError.getMessage();
        }
        return jsonRpcError.getMessage() + " : " + jsonRpcError.getData().get("detail");
    }

    public JsonRpcError getError() {
        return jsonRpcError;
    }
}
