package com.github.dnault.therapi.jsonrpc.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.dnault.therapi.jsonrpc.JsonRpcDispatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public abstract class AbstractJsonRpcServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract JsonRpcDispatcher getJsonRpcDispatcher();
    protected abstract ObjectWriter getObjectWriter();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<JsonNode> response = getJsonRpcDispatcher().invoke(req.getInputStream());

        if (response.isPresent()) {
            resp.setContentType("application/json");
            getObjectWriter().writeValue(resp.getOutputStream(), response.get());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<JsonNode> response = getJsonRpcDispatcher().invoke(req.getParameter("r"));

        if (response.isPresent()) {
            resp.setContentType("application/json");
            getObjectWriter().writeValue(resp.getOutputStream(), response.get());
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
