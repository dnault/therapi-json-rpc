package com.github.therapi.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.example.calculator.CalculatorServiceImpl;
import com.github.therapi.example.devilsdictionary.DictionaryServiceImpl;
import com.github.therapi.jsonrpc.JsonRpcDispatcher;
import com.github.therapi.jsonrpc.web.AbstractJsonRpcServlet;
import com.github.therapi.jsonrpc.web.JsonRpcServletHandler;
import com.github.therapi.jsonrpc.web.JsonRpcServletHandler.ResponseFormat;

import javax.servlet.ServletException;

import static com.github.therapi.jackson.ObjectMappers.newLenientObjectMapper;

public class ExampleJsonRpcServlet extends AbstractJsonRpcServlet {
    @Override
    public void init() throws ServletException {
        super.init();

        ObjectMapper objectMapper = newLenientObjectMapper();

        MethodRegistry registry = new MethodRegistry(objectMapper);
        registry.scan(new CalculatorServiceImpl());
        registry.scan(new DictionaryServiceImpl());

        JsonRpcDispatcher dispatcher = JsonRpcDispatcher.builder(registry).build();

        setHandler(new JsonRpcServletHandler(dispatcher, ResponseFormat.PRETTY));
    }
}
