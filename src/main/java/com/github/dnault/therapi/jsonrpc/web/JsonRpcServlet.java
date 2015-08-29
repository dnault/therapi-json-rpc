package com.github.dnault.therapi.jsonrpc.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.dnault.therapi.core.MethodRegistry;
import com.github.dnault.therapi.example.CalculatorServiceImpl;
import com.github.dnault.therapi.jsonrpc.JsonRpcDispatcher;
import com.github.dnault.therapi.jsonrpc.JsonRpcDispatcherImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;

import static com.github.dnault.therapi.core.internal.JacksonHelper.newLenientObjectMapper;

public class JsonRpcServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MethodRegistry registry = new MethodRegistry(newLenientObjectMapper());
        registry.scan(new CalculatorServiceImpl());

        JsonRpcDispatcher dispatcher = new JsonRpcDispatcherImpl(registry, Executors.newCachedThreadPool());
        Optional<JsonNode> response = dispatcher.invoke(req.getInputStream());

        if (response.isPresent()) {
            ObjectWriter prettyWriter = registry.getObjectMapper().writerWithDefaultPrettyPrinter();
            resp.setContentType("application/json");
            prettyWriter.writeValue(resp.getOutputStream(), response.get());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MethodRegistry registry = new MethodRegistry(newLenientObjectMapper());
        registry.scan(new CalculatorServiceImpl());

        JsonRpcDispatcher dispatcher = new JsonRpcDispatcherImpl(registry, Executors.newCachedThreadPool());
        Optional<JsonNode> response = dispatcher.invoke(req.getParameter("r"));

        if (response.isPresent()) {
            ObjectWriter prettyWriter = registry.getObjectMapper().writerWithDefaultPrettyPrinter();
            resp.setContentType("application/json");
            prettyWriter.writeValue(resp.getOutputStream(), response.get());
        }

        /*
        String request = req.getParameter("r");
        if (request != null) {
            Request r = new Request();
            r.setVersion("2.0");
            r.setId(new TextNode(""));

            int paramStartIndex = getParamStartIndex(request);
            final String params;
            final String method;

            if (paramStartIndex == -1) {
                params = "{}";
                method = request.trim();
            } else {
                params = request.substring(paramStartIndex);
                method = request.substring(0, paramStartIndex).trim();
            }

            resp.getWriter().println("method: " + method);
            resp.getWriter().println("params: " + params);
        }

*/
    }

    private int getParamStartIndex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[' || c == '{') {
                return i;
            }
        }
        return -1;
    }

}
