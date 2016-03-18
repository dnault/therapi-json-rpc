<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript,
                 static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3"%>
<%
    String methodName = (String) request.getAttribute("methodName");
    String schema = (String) request.getAttribute("schema");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">

    <link rel="stylesheet" href='../../therapi/json-forms/css/brutusin-json-forms.min.css'/>
    <script src="../../therapi/json-forms/js/brutusin-json-forms.min.js"></script>

    <script>
        function generateForm() {
            var schema = "<%= escapeEcmaScript(schema) %>"
            var BrutusinForms = brutusin["json-forms"];
            var bf = BrutusinForms.create(JSON.parse(schema));

            var container = document.getElementById('formContainer');
            bf.render(container);
       }
   </script>

</head>
<body onload="generateForm()">
<h2><%= escapeHtml3(methodName) %></h2>
    <div id="formContainer"></div>
</body>
