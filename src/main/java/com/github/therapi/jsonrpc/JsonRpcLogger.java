package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodDefinition;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Logs JSON-RPC requests and responses.
 */
public interface JsonRpcLogger {
    Logger log = LoggerFactory.getLogger(JsonRpcLogger.class);

    interface RequestInfo {
        /**
         * Returns the method name requested by the client
         */
        String getMethodName();

        /**
         * Returns the method definition being invoked, or {@code Optional.empty()} if method name was not recognized.
         */
        Optional<MethodDefinition> getMethodDefinition();

        /**
         * Returns the method arguments (may be Array or Object)
         */
        JsonNode getArguments();
    }

    interface ResponseInfo {
        /**
         * Returns the full JSON-RPC response
         */
        ObjectNode getResponse();

        /**
         * Returns the time elapsed during method invocation
         */
        Stopwatch getExecutionTimer();
    }

    void logRequest(RequestInfo requestInfo);

    void logSuccessResponse(RequestInfo requestInfo, ResponseInfo responseInfo);

    void logException(Throwable t);

    void logErrorResponse(RequestInfo requestInfo, ResponseInfo responseInfo);
}
