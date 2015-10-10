package com.github.therapi.example;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.example.devilsdictionary.DictionaryServiceImpl;
import com.github.therapi.jsonrpc.JsonRpcDispatcher;
import com.github.therapi.jsonrpc.JsonRpcDispatcherImpl;
import com.github.therapi.jsonrpc.web.AbstractJsonRpcServlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.Executors;

import static com.github.therapi.core.internal.JacksonHelper.newLenientObjectMapper;

public class ExampleJsonRpcServlet extends AbstractJsonRpcServlet {
    private JsonRpcDispatcher dispatcher;
    private MethodRegistry registry;
    private ObjectWriter objectWriter;

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            registry = new MethodRegistry(newLenientObjectMapper());
            registry.scan(new CalculatorServiceImpl());
            registry.scan(new DictionaryServiceImpl());

            this.dispatcher = new JsonRpcDispatcherImpl(registry, Executors.newCachedThreadPool());

            this.objectWriter = registry.getObjectMapper().writerWithDefaultPrettyPrinter();

        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected JsonRpcDispatcher getJsonRpcDispatcher() {
        return dispatcher;
    }

    @Override
    protected ObjectWriter getObjectWriter() {
        return objectWriter;
    }

    @Override
    protected MethodRegistry getMethodRegistry() {
        return registry;
    }
}
