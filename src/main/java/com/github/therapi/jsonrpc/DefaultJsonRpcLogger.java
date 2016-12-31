package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodDefinition;
import com.google.common.base.Stopwatch;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class DefaultJsonRpcLogger implements JsonRpcLogger {

    protected LogLevel getRequestLogLevel() {
        return LogLevel.INFO;
    }

    protected LogLevel getResponseTimeLogLevel() {
        return LogLevel.INFO;
    }

    protected LogLevel getSuccessfulResponseBodyLogLevel() {
        return LogLevel.DEBUG;
    }

    protected LogLevel getErrorResponseBodyLogLevel() {
        return LogLevel.INFO;
    }

    public void logRequest(@Nullable MethodDefinition methodDef, String methodName, JsonNode arguments) {
        final boolean loggable = methodDef != null && methodDef.isRequestLoggable();
        getRequestLogLevel().log(log, "Invoking '{}' {}", methodName, loggable ? arguments : "(args not loggable)");
    }

    public void logSuccessResponse(MethodDefinition methodDef, String methodName,
            JsonNode arguments, ObjectNode response, Stopwatch executionTimer) {

        getResponseTimeLogLevel().log(log, "'{}' completed successfully in {}", methodName, executionTimer);

        final boolean loggable = methodDef != null && methodDef.isResponseLoggable();
        getSuccessfulResponseBodyLogLevel().log(log, "'{}' response: {}", methodName,
                loggable ? response : "(not loggable)");
    }

    public void logErrorResponse(@Nullable MethodDefinition methodDef, String methodName,
            JsonNode arguments, ObjectNode response, Stopwatch executionTimer) {

        getResponseTimeLogLevel().log(log, "'{}' completed with error in {}", methodName, executionTimer);

        // note that error responses are logged regardless of whether the method was annotated @DoNotLog
        getErrorResponseBodyLogLevel().log(log, "'{}' error response: {}", methodName, response);
    }

    protected enum LogLevel {
        ERROR {
            @Override public void log(Logger logger, String message, Object... args) {
                logger.error(message, args);
            }
        }, WARN {
            @Override public void log(Logger logger, String message, Object... args) {
                logger.warn(message, args);
            }
        }, INFO {
            @Override public void log(Logger logger, String message, Object... args) {
                logger.info(message, args);
            }
        }, DEBUG {
            @Override public void log(Logger logger, String message, Object... args) {
                logger.debug(message, args);
            }
        };

        public abstract void log(Logger logger, String message, Object... args);
    }
}
