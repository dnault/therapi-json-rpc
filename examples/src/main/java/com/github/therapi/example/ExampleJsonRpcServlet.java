package com.github.therapi.example;

import static com.github.therapi.core.internal.JacksonHelper.newLenientObjectMapper;

import javax.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.example.devilsdictionary.DictionaryServiceImpl;
import com.github.therapi.jackson.enums.LowerCamelCaseEnumModule;
import com.github.therapi.jsonrpc.DefaultExceptionTranslator;
import com.github.therapi.jsonrpc.web.AbstractJsonRpcServlet;
import com.github.therapi.jsonrpc.web.JsonRpcServletHandler;

public class ExampleJsonRpcServlet extends AbstractJsonRpcServlet {
    @Override
    public void init() throws ServletException {
        super.init();

        ObjectMapper objectMapper = newLenientObjectMapper();
        objectMapper.registerModule(new LowerCamelCaseEnumModule());

        MethodRegistry registry = new MethodRegistry(objectMapper);
        registry.scan(new CalculatorServiceImpl());
        registry.scan(new DictionaryServiceImpl());

        setHandler(new JsonRpcServletHandler(registry, new DefaultExceptionTranslator()));
    }
}
