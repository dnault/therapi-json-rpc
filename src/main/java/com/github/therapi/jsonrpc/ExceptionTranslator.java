package com.github.therapi.jsonrpc;

public interface ExceptionTranslator {
    JsonRpcError translate(Throwable t);
}
