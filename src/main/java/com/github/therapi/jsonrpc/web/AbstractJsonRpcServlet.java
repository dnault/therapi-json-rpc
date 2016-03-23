package com.github.therapi.jsonrpc.web;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.removeStart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.github.therapi.apidoc.ApiDocProvider;
import com.github.therapi.apidoc.JsonSchemaProvider;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.MethodDefinition;
import com.github.therapi.core.ParameterDefinition;
import com.github.therapi.core.internal.TypesHelper;

public abstract class AbstractJsonRpcServlet extends HttpServlet {

    private JsonRpcServletHandler handler;

    protected void setHandler(JsonRpcServletHandler handler) {
        this.handler = requireNonNull(handler);
    }

    /**
     * @throws IllegalStateException if the handler has not been set
     */
    protected JsonRpcServletHandler getHandler() {
        checkState(handler != null, "handler not initialized; must call setHandler first");
        return handler;
    }

    protected MethodRegistry getMethodRegistry() {
        return getHandler().getRegistry();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getHandler().handlePost(req, resp);
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

            if (pathInfo.startsWith("/ui/")) {
                sendUi(req, resp, removeStart(pathInfo, "/ui/"));
                return;
            }

            if ("/client.js".equals(req.getPathInfo())) {
                sendJavascriptClient(req, resp);
                return;
            }
        }

        getHandler().handleGet(req, resp);
    }

    protected void sendApiDoc(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        ApiDocProvider provider = new ApiDocProvider();
        req.setAttribute("therapiNamespaces", provider.getDocumentation(getMethodRegistry()));
        req.getRequestDispatcher("/WEB-INF/therapi/apidoc.jsp").forward(req, resp);
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

    protected void sendUi(HttpServletRequest req, HttpServletResponse resp, String methodName) throws IOException, ServletException {

        MethodDefinition methodDef = getMethodRegistry().getMethod(methodName).orElse(null);
        if (methodDef == null) {
            resp.sendError(404, "Method not found: " + methodName);
            return;
        }

        String schema = new JsonSchemaProvider().getSchema(getMethodRegistry().getObjectMapper(), methodDef);

        req.setAttribute("schema", schema);
        req.setAttribute("methodName", methodName);

        req.getRequestDispatcher("/WEB-INF/therapi/ui.jsp").forward(req, resp);
    }

    private void sendJavascriptClient(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // "application/javascript" is more correct, but might alienate IE8
        resp.setContentType("text/javascript; charset=UTF-8");

        PrintWriter writer = resp.getWriter();

        // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error#Custom_Error_Types
        writer.println("\n"
                + "function JsonRpcError(jsonRpcErrorResponse) {\n"
                + "    this.name = \"JsonRpcError\";\n"
                + "    this.code = jsonRpcErrorResponse.code;\n"
                + "    this.message = jsonRpcErrorResponse.message;\n"
                + "    this.data = jsonRpcErrorResponse.data;\n"
                + "    this.stack = (new Error()).stack;\n"
                + "}\n"
                + "JsonRpcError.prototype = Object.create(Error.prototype);\n"
                + "JsonRpcError.prototype.constructor = JsonRpcError;");

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
            writer.println("    return new Promise(function (resolve, reject) {\n"
                    + "        $.jsonRPC.request('" + mdef.getQualifiedName(".") + "', {\n"
                    + "            params: {" + paramMap + "},\n"
                    + "            success: function (result) {\n"
                    // + "                console.debug(JSON.stringify(result));\n"
                    + "                resolve(result.result);\n"
                    + "            },\n"
                    + "            error: function (result) {\n"
                    // + "                console.warn(JSON.stringify(result));\n"
                    + "                reject(new JsonRpcError(result.error));\n"
                    + "            }\n"
                    + "        });\n"
                    + "    });\n};\n");

        }

    }
}
