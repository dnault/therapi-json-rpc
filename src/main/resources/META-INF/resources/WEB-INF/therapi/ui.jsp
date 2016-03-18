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

    <script type="text/javascript"
            src="https://cdn.jsdelivr.net/jquery/1.5.1/jquery.min.js"></script>


    <script type="text/javascript"
            src="../../script/datagraph/jquery-jsonrpc/0.1.1/jquery.jsonrpc.js"></script>

<!--
    <script type="text/javascript"
            src="../../script/jakearchibald/es6-promise-3.0.2/es6-promise.min.js"></script>
-->


    <script type="text/javascript">
        $.jsonRPC.setup({
            endPoint: '..',
        });
    </script>

    <script>
        function generateForm() {
            var schema = "<%= escapeEcmaScript(schema) %>"
            var BrutusinForms = brutusin["json-forms"];
            bf = BrutusinForms.create(JSON.parse(schema));

            var container = document.getElementById('formContainer');
            bf.render(container);
        }

        function JsonRpcError(jsonRpcErrorResponse) {
            this.name = "JsonRpcError";
            this.code = jsonRpcErrorResponse.code;
            this.message = jsonRpcErrorResponse.message;
            this.data = jsonRpcErrorResponse.data;
            this.stack = (new Error()).stack;
        }
        JsonRpcError.prototype = Object.create(Error.prototype);
        JsonRpcError.prototype.constructor = JsonRpcError;

        function invokeJsonRpc(name, params) {
            return new Promise(function (resolve, reject) {
                $.jsonRPC.request(name, {
                    params: params,
                    success: function (result) {
                        resolve(result.result);
                    },
                    error: function (result) {
                        reject(new JsonRpcError(result.error));
                    }
                });
            });
        }

        logit = function(x) { if (x instanceof JsonRpcError) { logerr(x);} else {alert(JSON.stringify(x, null, 2));} };
        logerr = function(x) { alert("error:" + JSON.stringify(x, null, 2)); if (x.data && x.data.detail) {console.warn(x.data.detail);}};
        //logit = function(x) { if (x instanceof JsonRpcError) { logerr(x);} else {console.debug(x);} };
        //logerr = function(x) { console.warn(x); if (x.data && x.data.detail) {console.warn(x.data.detail);}};
        rethrow = function(e) { throw e; };

   </script>

</head>
<body onload="generateForm()">
<h2><%= escapeHtml3(methodName) %></h2>
    <div id="formContainer"></div>

    <div class="panel-footer">
        <button class="btn btn-primary" onclick="alert(JSON.stringify(bf.getData(), null, 4))">getData()</button>
        &nbsp;
        <button class="btn btn-primary" onclick="if (bf.validate()) {alert('Validation succeeded')}">validate()</button>
        &nbsp;
        <button class="btn btn-primary" onclick="invokeJsonRpc('<%= escapeEcmaScript(methodName) %>', bf.getData()).then(logit).catch(logit)">invoke()</button>
    </div>
</body>
