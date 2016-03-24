package com.github.therapi.example.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.jsonrpc.web.AbstractSpringJsonRpcController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;


/**
 * The {@link AbstractSpringJsonRpcController} base class is not eligible
 * for component scanning, and does not know where to get an ObjectMapper.
 * You'll need a concrete subclass like this one to supply those missing features.
 */
@Controller
public class ExampleJsonRpcController extends AbstractSpringJsonRpcController {
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("jsonRpcObjectMapper")
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
