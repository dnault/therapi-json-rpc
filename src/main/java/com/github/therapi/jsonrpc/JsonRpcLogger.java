package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodDefinition;
import com.google.common.base.Stopwatch;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs JSON-RPC requests and responses.
 */
public interface JsonRpcLogger {
    Logger log = LoggerFactory.getLogger(JsonRpcLogger.class);

    /**
     * @param methodDef    will be null if the method name is not recognized
     * @param methodName   the method name requested by the client
     * @param arguments    the method arguments (may be Array or Object)
     */
    void logRequest(@Nullable MethodDefinition methodDef, String methodName, JsonNode arguments);

    /**
     * @param methodName     the method name requested by the client
     * @param arguments      the method arguments
     * @param response       the full JSON-RPC response
     * @param executionTimer time elapsed during method invocation
     */
    void logSuccessResponse(MethodDefinition methodDef, String methodName,
            JsonNode arguments, ObjectNode response, Stopwatch executionTimer);

    /**
     * @param methodDef      will be null if the method name is not recognized
     * @param methodName     the method name requested by the client
     * @param arguments      the method arguments
     * @param response       the full JSON-RPC response
     * @param executionTimer time elapsed during method invocation
     */
    void logErrorResponse(@Nullable MethodDefinition methodDef, String methodName, JsonNode arguments,
            ObjectNode response, Stopwatch executionTimer);
}
