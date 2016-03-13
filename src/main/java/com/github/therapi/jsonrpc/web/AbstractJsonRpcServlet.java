package com.github.therapi.jsonrpc.web;

import static org.apache.commons.lang3.StringUtils.removeStart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.therapi.apidoc.ApiDocProvider;
import com.github.therapi.apidoc.JsonSchemaProvider;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.internal.MethodDefinition;
import com.github.therapi.core.internal.ParameterDefinition;
import com.github.therapi.core.internal.TypesHelper;
import com.github.therapi.jsonrpc.JsonRpcDispatcher;

public abstract class AbstractJsonRpcServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract JsonRpcDispatcher getJsonRpcDispatcher();

    protected abstract ObjectWriter getObjectWriter();

    protected abstract MethodRegistry getMethodRegistry();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<JsonNode> response = getJsonRpcDispatcher().invoke(req.getInputStream());

        if (response.isPresent()) {
            resp.setContentType("application/json");
            getObjectWriter().writeValue(resp.getOutputStream(), response.get());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            if ("/apidoc".equals(req.getPathInfo())) {
                sendApiDoc(req, resp);
                return;
            }

            if (pathInfo.startsWith("/modeldoc/")) {
                sendModelDoc(req, resp, removeStart(pathInfo, "/modeldoc/"));
                return;
            }

            if ("/client.js".equals(req.getPathInfo())) {
                sendJavascriptClient(req, resp);
                return;
            }
        }

        String jsonRequest = req.getParameter("r");
        if (jsonRequest == null) {
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
            out.println("API documentation is <a href=\"" + req.getContextPath() + req.getServletPath() + "/apidoc\">over here</a>.");
            return;
        }

        Optional<JsonNode> response = getJsonRpcDispatcher().invoke(jsonRequest);

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

    protected void sendApiDoc(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        ApiDocProvider provider = new ApiDocProvider();
        req.setAttribute("therapiNamespaces", provider.getDocumentation(getMethodRegistry()));
        req.getRequestDispatcher("/WEB-INF/therapi/apidoc.jsp").include(req, resp);
    }

    protected void sendModelDoc(HttpServletRequest req, HttpServletResponse resp, String modelClassName) throws IOException, ServletException {
        Class modelClass = TypesHelper.findClass(modelClassName).orElse(null);

        if (modelClass == null) {
            resp.sendError(404, "Model class not found: " + modelClassName);
            return;
        }

        String schema = new JsonSchemaProvider().getSchema(getMethodRegistry().getObjectMapper(), modelClass).orElse(null);

        req.setAttribute("modelClassName", modelClassName);
        req.setAttribute("schema", schema);
        req.getRequestDispatcher("/WEB-INF/therapi/modeldoc.jsp").include(req, resp);
    }

    private void sendJavascriptClient(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // "application/javascript" is more correct, but might alienate IE8
        resp.setContentType("text/javascript; charset=UTF-8");

        PrintWriter writer = resp.getWriter();

        // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error#Custom_Error_Types
        writer.println("\n" +
                "function JsonRpcError(jsonRpcErrorResponse) {\n" +
                "    this.name = \"JsonRpcError\";\n" +
                "    this.code = jsonRpcErrorResponse.code;\n" +
                "    this.message = jsonRpcErrorResponse.message;\n" +
                "    this.data = jsonRpcErrorResponse.data;\n" +
                "    this.stack = (new Error()).stack;\n" +
                "}\n" +
                "JsonRpcError.prototype = Object.create(Error.prototype);\n" +
                "JsonRpcError.prototype.constructor = JsonRpcError;");

        writer.println();

        writer.println("logit = function(x) { if (x instanceof JsonRpcError) { logerr(x);} else {console.debug(x);} };");
        writer.println("logerr = function(x) { console.warn(x); if (x.data && x.data.detail) {console.warn(x.data.detail);}};");
        writer.println("rethrow = function(e) { throw e; };");

        writer.println();

        writer.println("Therapi = {}");

        Set<String> definedNamespaces = new HashSet<>();

        for (MethodDefinition mdef : getMethodRegistry().getMethods()) {

            writer.println("console.debug('" + mdef.getQualifiedName(".") + "')");

            String[] components = mdef.getQualifiedName(".").split("\\.");

            StringBuilder namespaceFragment = new StringBuilder();
            for (int i = 0; i < components.length - 1; i++) {
                if (i > 0) {
                    namespaceFragment.append(".");
                }
                namespaceFragment.append(components[i]);
                if (definedNamespaces.add(namespaceFragment.toString())) {
                    writer.println("Therapi." + namespaceFragment + " = {}");
                }
            }

            writer.print("Therapi." + mdef.getQualifiedName(".") + " = function(");
            int i = 0;
            StringBuilder paramMap = new StringBuilder();
            for (ParameterDefinition pdef : mdef.getParameters()) {
                writer.print(pdef.getName());
                paramMap.append(pdef.getName() + ": " + pdef.getName());
                if (++i < mdef.getParameters().size()) {
                    writer.print(", ");
                    paramMap.append(", ");
                }
            }


            writer.println(") {");


            /*

            writer.println("    $.jsonRPC.request('" + mdef.getQualifiedName(".") + "', {\n" +
                    "        params: {" + paramMap + "},\n" +
                    "        success: function (result) {\n" +
                    "            console.debug(JSON.stringify(result))\n" +
                    "        },\n" +
                    "        error: function (result) {\n" +
                    "            console.warn(JSON.stringify(result))\n" +
                    "        }\n" +
                    "    });\n" +
                    "};");
*/
            writer.println("    return new Promise(function (resolve, reject) {\n" +
                    "        $.jsonRPC.request('" + mdef.getQualifiedName(".") + "', {\n" +
                    "            params: {" + paramMap + "},\n" +
                    "            success: function (result) {\n" +
                    //"                console.debug(JSON.stringify(result));\n" +
                    "                resolve(result.result);\n" +
                    "            },\n" +
                    "            error: function (result) {\n" +
                    //"                console.warn(JSON.stringify(result));\n" +
                    "                reject(new JsonRpcError(result.error));\n" +
                    "            }\n" +
                    "        });\n" +
                    "    });\n};\n");


        }

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
