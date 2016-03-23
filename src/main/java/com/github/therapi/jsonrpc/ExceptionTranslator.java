package com.github.therapi.jsonrpc;

/**
 * Translates exceptions into JSON-RPC error responses.
 */
public interface ExceptionTranslator {
    JsonRpcError translate(Throwable t);
}
