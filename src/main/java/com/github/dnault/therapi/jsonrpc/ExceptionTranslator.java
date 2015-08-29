package com.github.dnault.therapi.jsonrpc;

public interface ExceptionTranslator {
    JsonRpcError translate(Throwable t);
}
