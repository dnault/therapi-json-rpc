package com.github.therapi.jsonrpc;

import com.github.therapi.core.MethodRegistry;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * Create new instances using the {@link JsonRpcDispatcher#builder} factory method.
 */
public class JsonRpcDispatcherBuilder {
    private final MethodRegistry registry;
    private ExceptionTranslator exceptionTranslator = new DefaultExceptionTranslator();
    private JsonRpcLogger logger = new DefaultJsonRpcLogger();
    private ExecutorService executorService = MoreExecutors.newDirectExecutorService();

    JsonRpcDispatcherBuilder(MethodRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    /**
     * Specifies how Java exceptions will be translated to JSON-RPC error nodes.
     * By default all exceptions are treated as internal server errors.
     *
     * @see DefaultExceptionTranslator
     */
    public JsonRpcDispatcherBuilder exceptionTranslator(ExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = requireNonNull(exceptionTranslator);
        return this;
    }

    /**
     * Specifies the executor service used to invoke methods in batch requests.
     * By default all methods in a batch are executed serially by the current thread.
     */
    public JsonRpcDispatcherBuilder batchExecutorService(ExecutorService executorService) {
        this.executorService = requireNonNull(executorService);
        return this;
    }

    /**
     * Specifies how requests, responses, and exceptions are logged.
     *
     * @see DefaultJsonRpcLogger
     */
    public JsonRpcDispatcherBuilder logger(JsonRpcLogger logger) {
        this.logger = requireNonNull(logger);
        return this;
    }

    public JsonRpcDispatcher build() {
        return new JsonRpcDispatcherImpl(registry, exceptionTranslator, executorService, logger);
    }
}
