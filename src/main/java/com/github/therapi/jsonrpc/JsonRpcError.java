package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {
    private final int code;
    private final String message;
    private Map<String, Object> data;

    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = requireNonNull(message);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
