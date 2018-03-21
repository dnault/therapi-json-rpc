package com.github.therapi.jsonrpc.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * When deploying to servlet containers that do not support Servlet API 3.0
 * (Google App Engine, I'm looking at you) this servlet serves up the resources
 * that would normally be loaded automatically from the META-INF/resources
 * directory inside the Therapi JAR.
 * <p>
 * Add the following snippet to your web.xml and you should be good to go.
 * <pre>
 * &lt;servlet&gt;
 *     &lt;servlet-name&gt;MetaInfResourceExporterServlet&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;com.github.therapi.jsonrpc.web.MetaInfResourceExporterServlet&lt;/servlet-class&gt;
 *     &lt;load-on-startup&gt;0&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;MetaInfResourceExporterServlet&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;/therapi/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 */
public class MetaInfResourceExporterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        String resourceName = "/META-INF/resources/therapi" + pathInfo;

        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            if (is == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, resourceName);
                return;
            }

            String contentType = null;
            if (resourceName.endsWith(".js")) {
                contentType = "text/javascript";
            } else if (resourceName.endsWith(".css")) {
                contentType = "text/css";
            } else if (resourceName.endsWith(".png")) {
                contentType = "image/png";
            }

            if (contentType != null) {
                response.setContentType(contentType);
            }

            try (OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
        }
    }
}