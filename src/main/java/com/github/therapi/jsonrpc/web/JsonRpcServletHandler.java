package com.github.therapi.jsonrpc.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.jsonrpc.JsonRpcDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static com.github.therapi.jsonrpc.web.JsonRpcServletHandler.ResponseFormat.COMPACT;
import static com.github.therapi.jsonrpc.web.JsonRpcServletHandler.ResponseFormat.PRETTY;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;

/**
 * A servlet or Spring Controller may delegate to this handler to support JSON-RPC via servlet request/response.
 */
public class JsonRpcServletHandler {
    private final JsonRpcDispatcher dispatcher;
    private final ObjectWriter compactWriter;
    private final ObjectWriter prettyPrintWriter;
    private final ResponseFormat defaultResponseFormat;

    public enum ResponseFormat {
        COMPACT, PRETTY
    }

    public JsonRpcServletHandler(JsonRpcDispatcher dispatcher, ResponseFormat defaultResponseFormat) {
        this.dispatcher = requireNonNull(dispatcher);
        this.defaultResponseFormat = requireNonNull(defaultResponseFormat);

        ObjectMapper mapper = dispatcher.getMethodRegistry().getObjectMapper();
        this.compactWriter = mapper.writer().without(SerializationFeature.INDENT_OUTPUT);
        this.prettyPrintWriter = mapper.writerWithDefaultPrettyPrinter();
    }

    public MethodRegistry getRegistry() {
        return dispatcher.getMethodRegistry();
    }

    public void handlePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendResponse(req, resp, dispatcher.invoke(req.getInputStream()));
    }

    public void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jsonRequest = req.getParameter("r");

        if (isEmpty(jsonRequest)) {
            sendGreeting(req, resp);
            return;
        }

        sendResponse(req, resp, dispatcher.invoke(jsonRequest));
    }

    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp, Optional<JsonNode> response) throws IOException {
        if (response.isPresent()) {
            setResponseHeaders(resp);
            ObjectWriter responseWriter = getResponseFormat(req).orElse(defaultResponseFormat) == COMPACT
                    ? compactWriter : prettyPrintWriter;

            responseWriter.writeValue(resp.getOutputStream(), response.get());
        }
    }

    protected Optional<ResponseFormat> getResponseFormat(HttpServletRequest request) {
        String prettyPrintHeader = request.getHeader("X-Pretty-Print");
        return prettyPrintHeader == null
                ? Optional.empty()
                : Optional.of(prettyPrintHeader.equals("false") ? COMPACT : PRETTY);
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
