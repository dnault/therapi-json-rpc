package com.github.dnault.therapi.example;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.dnault.therapi.core.MethodRegistry;
import com.github.dnault.therapi.jsonrpc.JsonRpcDispatcher;
import com.github.dnault.therapi.jsonrpc.JsonRpcDispatcherImpl;
import com.github.dnault.therapi.jsonrpc.web.AbstractJsonRpcServlet;

import javax.servlet.ServletException;
import java.util.concurrent.Executors;

import static com.github.dnault.therapi.core.internal.JacksonHelper.newLenientObjectMapper;

public class ExampleJsonRpcServlet extends AbstractJsonRpcServlet {
    private JsonRpcDispatcher dispatcher;
    private ObjectWriter objectWriter;

    @Override
    public void init() throws ServletException {
        super.init();

        MethodRegistry registry = new MethodRegistry(newLenientObjectMapper());
        registry.scan(new CalculatorServiceImpl());

        this.dispatcher = new JsonRpcDispatcherImpl(registry, Executors.newCachedThreadPool());

        this.objectWriter = registry.getObjectMapper().writerWithDefaultPrettyPrinter();
    }

    @Override
    protected JsonRpcDispatcher getJsonRpcDispatcher() {
        return dispatcher;
    }

    @Override
    protected ObjectWriter getObjectWriter() {
        return objectWriter;
    }
}
