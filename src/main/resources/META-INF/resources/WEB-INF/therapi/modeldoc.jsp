<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.github.therapi.runtimejavadoc.RuntimeJavadocReader" %>
<%@ page import="com.github.therapi.runtimejavadoc.ClassJavadoc" %>
<%@ page import="com.github.therapi.runtimejavadoc.CommentFormatter" %>
<%@ page import="com.github.therapi.apidoc.TherapiNamespaceDoc" %>
<%@ page import="com.github.therapi.apidoc.TherapiParamDoc" %>
<%@ page import="com.github.therapi.apidoc.ApiDocProvider" %>
<%@ page import="static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3" %>
<%@ page import="java.util.List" %>
<%@ page import="static com.google.common.base.Strings.nullToEmpty" %>

model: "<%= request.getAttribute("modelClassName") %>"

<% ClassJavadoc classDoc = new RuntimeJavadocReader().getDocumentation(((String) request.getAttribute("modelClassName")));
if (classDoc != null) {
  out.println(escapeHtml3(new CommentFormatter().format(classDoc.getComment())));
}
%>