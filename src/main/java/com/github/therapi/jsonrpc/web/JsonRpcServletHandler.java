package com.github.therapi.jsonrpc.web;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;

import com.github.therapi.jsonrpc.DefaultJsonRpcLogger;
import com.github.therapi.jsonrpc.JsonRpcLogger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.jsonrpc.ExceptionTranslator;
import com.github.therapi.jsonrpc.JsonRpcDispatcher;
import com.github.therapi.jsonrpc.JsonRpcDispatcherImpl;
import com.google.common.util.concurrent.MoreExecutors;

public class JsonRpcServletHandler {
    private final MethodRegistry registry;
    private final JsonRpcDispatcher dispatcher;
    private final ObjectWriter objectWriter;

    /**
     * Constructs a handler that executes batched requests serially
     * in the same thread that invokes the handler.
     */
    public JsonRpcServletHandler(MethodRegistry registry,
                                 ExceptionTranslator translator) {
        this(registry, translator, MoreExecutors.newDirectExecutorService(), new DefaultJsonRpcLogger());
    }

    /**
     * Constructs a handler that executes batched requests using the given executor service.
     *
     * @param registry the methods to expose
     */
    public JsonRpcServletHandler(MethodRegistry registry,
                                 ExceptionTranslator translator,
                                 ExecutorService executorService,
                                 JsonRpcLogger jsonRpcLogger) {
        this(registry,
                new JsonRpcDispatcherImpl(registry, translator, executorService, jsonRpcLogger),
                registry.getObjectMapper().writerWithDefaultPrettyPrinter());
    }

    /**
     * Constructs a handler that executes requests using the given customizable components.
     */
    public JsonRpcServletHandler(MethodRegistry registry,
                                 JsonRpcDispatcher dispatcher,
                                 ObjectWriter objectWriter) {
        this.registry = requireNonNull(registry);
        this.dispatcher = requireNonNull(dispatcher);
        this.objectWriter = requireNonNull(objectWriter);
    }

    public MethodRegistry getRegistry() {
        return registry;
    }

    public void handlePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendResponse(resp, dispatcher.invoke(req.getInputStream()));
    }

    public void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jsonRequest = req.getParameter("r");

        if (isEmpty(jsonRequest)) {
            sendGreeting(req, resp);
            return;
        }

        sendResponse(resp, dispatcher.invoke(jsonRequest));
    }

    protected void sendResponse(HttpServletResponse resp, Optional<JsonNode> response) throws IOException {
        if (response.isPresent()) {
            setResponseHeaders(resp);
            objectWriter.writeValue(resp.getOutputStream(), response.get());
        }
    }

    protected void setResponseHeaders(HttpServletResponse resp) {
        resp.setContentType("application/json");
    }

    protected void sendGreeting(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(400);
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        out.println("Hi. This URI is a <a href=\"http://www.jsonrpc.org/specification\">JSON-RPC 2.0</a> endpoint.");
        out.println("<p>");
        out.println("Clients should submit request objects in the body of a POST to this URI.");
        out.println("If you just want to poke around, you can manually submit a request object as the 'r' query parameter of a GET request.");
        out.println("Don't forget the 'id' property of your request object, otherwise it will be treated as a notification and you won't see the response.");
        out.println("<p>");
        out.println("API documentation is <a href=\"" + req.getContextPath() + removeEnd(req.getServletPath(), "/") + "/apidoc\">over here</a>.");
    }
}
