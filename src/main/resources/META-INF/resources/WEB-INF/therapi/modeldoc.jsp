<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.github.therapi.runtimejavadoc.RuntimeJavadocReader" %>
<%@ page import="com.github.therapi.runtimejavadoc.ClassJavadoc" %>
<%@ page import="com.github.therapi.runtimejavadoc.CommentFormatter" %>
<%@ page import="com.github.therapi.apidoc.ApiNamespaceDoc" %>
<%@ page import="com.github.therapi.apidoc.ApiParamDoc" %>
<%@ page import="com.github.therapi.apidoc.ApiDocProvider" %>
<%@ page import="static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3" %>
<%@ page import="static com.github.therapi.core.internal.TypesHelper.findClass" %>
<%@ page import="java.util.List" %>
<%@ page import="static com.google.common.base.Strings.nullToEmpty" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String modelClassName = (String) request.getAttribute("modelClassName");
    String schema = (String) request.getAttribute("schema");
%>

<html>
<body>
<h2><%= request.getAttribute("modelClassName") %></h2>

<%
    ClassJavadoc classDoc = new RuntimeJavadocReader().getDocumentation(modelClassName);
    if (classDoc != null) {
        out.println("<h2>Description</h2>");
        out.println(escapeHtml3(new CommentFormatter().format(classDoc.getComment())));
    }

    if (schema != null) { %>
<h2>Schema</h2>
<pre>
<%= schema %>
</pre>
<%  } %>

</body>
</html>