package com.github.therapi.jsonrpc;

import com.github.therapi.core.MethodDefinition;
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

    protected LogLevel getExceptionLogLevel() {
        return LogLevel.WARN;
    }

    @Override
    public void logException(Throwable t) {
        getExceptionLogLevel().log(log, "Exception thrown during json-rpc invocation", t);
    }

    @Override
    public void logRequest(RequestInfo requestInfo) {
        MethodDefinition methodDef = requestInfo.getMethodDefinition().orElse(null);
        final boolean loggable = methodDef != null && methodDef.isRequestLoggable();
        getRequestLogLevel().log(log, "Invoking '{}' {}",
                requestInfo.getMethodName(), loggable ? requestInfo.getArguments() : "(args not loggable)");
    }

    @Override
    public void logSuccessResponse(RequestInfo requestInfo, ResponseInfo responseInfo) {
        String methodName = requestInfo.getMethodName();
        MethodDefinition methodDef = requestInfo.getMethodDefinition().orElse(null);

        getResponseTimeLogLevel().log(log, "'{}' completed successfully in {}", methodName, responseInfo.getExecutionTimer());

        final boolean loggable = methodDef != null && methodDef.isResponseLoggable();
        getSuccessfulResponseBodyLogLevel().log(log, "'{}' response: {}", methodName,
                loggable ? responseInfo.getResponse() : "(not loggable)");
    }

    @Override
    public void logErrorResponse(RequestInfo requestInfo, ResponseInfo responseInfo) {
        String methodName = requestInfo.getMethodName();
        getResponseTimeLogLevel().log(log, "'{}' completed with error in {}", methodName, responseInfo.getExecutionTimer());

        // note that error responses are logged regardless of whether the method was annotated @DoNotLog
        getErrorResponseBodyLogLevel().log(log, "'{}' error response: {}", methodName, responseInfo.getResponse());
    }

    protected enum LogLevel {
        ERROR {
            @Override
            public void log(Logger logger, String message, Object... args) {
                logger.error(message, args);
            }
        }, WARN {
            @Override
            public void log(Logger logger, String message, Object... args) {
                logger.warn(message, args);
            }
        }, INFO {
            @Override
            public void log(Logger logger, String message, Object... args) {
                logger.info(message, args);
            }
        }, DEBUG {
            @Override
            public void log(Logger logger, String message, Object... args) {
                logger.debug(message, args);
            }
        };

        public abstract void log(Logger logger, String message, Object... args);
    }
}
