<%@ page import="com.github.therapi.apidoc.ApiMethodDoc" %>
<%@ page import="com.github.therapi.apidoc.ApiNamespaceDoc" %>
<%@ page import="com.github.therapi.apidoc.ApiParamDoc" %>
<%@ page import="com.github.therapi.apidoc.ApiDocProvider" %>
<%@ page import="static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3" %>
<%@ page import="static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript" %>
<%@ page import="java.util.List" %>
<%@ page import="static com.google.common.base.Strings.nullToEmpty" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="../therapi/cssmenu/styles.css">
    <script src="https://code.jquery.com/jquery-1.11.3.min.js" type="text/javascript"></script>
    <script src="../therapi/cssmenu/script.js"></script>

    <link rel="stylesheet" href='../therapi/json-forms/css/brutusin-json-forms.min.css'/>
    <script src="../therapi/json-forms/js/brutusin-json-forms.min.js"></script>

    <link rel="stylesheet" href="../therapi/highlight/styles/github-gist.css">
    <script src="../therapi/highlight/highlight.pack.js"></script>

    <script type="text/javascript"
            src="../therapi/datagraph/jquery-jsonrpc/0.1.1/jquery.jsonrpc.js"></script>

    <script type="text/javascript"
            src="../therapi/jakearchibald/es6-promise-3.0.2/es6-promise.min.js"></script>

    <script type="text/javascript">
        $.jsonRPC.setup({
            endPoint: '../jsonrpc',
        });
    </script>

    <title>API Documentation</title>
</head>
<body>
<style>
    .container {
        position: relative;
    }

    #sidebar {
        position: fixed;
        left: 10px;
        top: 10px; /* change to adjust height from the top of the page */
    }

    #content {
        margin-left: 275px;
        margin-right: 10px;
    }

    table {
        border-collapse: collapse;
        width: 100%;
        margin: 0 0 20px 0;
    }

    caption {
        text-align: left;
        font-family: "Source Sans Pro", sans-serif;
        font-weight: 700;
        padding: 2px 0px;
    }

    th {
        background-color: #f5f5f5;
        text-align: left;
        font-family: "Source Sans Pro", sans-serif;
        font-weight: 700;
        padding: 4px 8px;
        border: #e0e0e0 1px solid;
        font-size: 90%;
    }

    td {
        vertical-align: top;
        padding: 2px 8px;
        border: #e0e0e0 1px solid;
    }

    h2 {
        border-top-left-radius: 9px;
        border-top-right-radius: 9px;

        padding: 5px;
        padding-left: 10px;

        color: white;
        text-shadow: 1px 1px 2px rgba(0, 0, 0, 1);

        /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#3b679e+0,2b88d9+8,207cca+15,7db9e8+100 */
        background: rgb(59,103,158); /* Old browsers */
        background: -moz-linear-gradient(top,  rgba(59,103,158,1) 0%, rgba(43,136,217,1) 8%, rgba(32,124,202,1) 15%, rgba(125,185,232,1) 100%); /* FF3.6-15 */
        background: -webkit-linear-gradient(top,  rgba(59,103,158,1) 0%,rgba(43,136,217,1) 8%,rgba(32,124,202,1) 15%,rgba(125,185,232,1) 100%); /* Chrome10-25,Safari5.1-6 */
        background: linear-gradient(to bottom,  rgba(59,103,158,1) 0%,rgba(43,136,217,1) 8%,rgba(32,124,202,1) 15%,rgba(125,185,232,1) 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#3b679e', endColorstr='#7db9e8',GradientType=0 ); /* IE6-9 */
    }


</style>

<div class="container">
    <div id="sidebar">
        <div id="cssmenu">
            <ul>
                <!-- <li class="active"><a href="#"><span>Home</span></a></li> -->

                <% for (ApiNamespaceDoc nsDoc : (List<ApiNamespaceDoc>) request.getAttribute("therapiNamespaces")) { %>
                <li class="has-sub"><a href="#"><span><%= nsDoc.getName() %></span></a>
                    <ul>
                        <% for (ApiMethodDoc methodDoc : nsDoc.getMethods()) { %>
                        <li>
                            <a href="#<%=nsDoc.getName() + "." + methodDoc.getName()%>">
                                <span><%= methodDoc.getName() %></span>
                            </a>
                        </li>
                        <% } %>
                    </ul>
                </li>
                <% } %>
            </ul>
        </div>
    </div>
    <div id="header"></div>
    <div id="content">

        <% for (ApiNamespaceDoc nsDoc : (List<ApiNamespaceDoc>) request.getAttribute("therapiNamespaces")) { %>
        <a name="<%=nsDoc.getName()%>"></a>

        <%--
        <h1>
            <%=nsDoc.getName()%>
        </h1>
        --%>

        <% for (ApiMethodDoc methodDoc : nsDoc.getMethods()) {
               String methodName = nsDoc.getName() + "." + methodDoc.getName();
               String formContainerId = ("formContainer." + methodName).replace(".","_");
               String tryItButtonId = ("tryIt." + methodName).replace(".","_");
               String explorerId = ("explorer." + methodName).replace(".","_");
               String formVar = ("form." + methodName).replace(".","_");
               String requestContainerId = ("request." + methodName).replace(".","_");
               String responseContainerId = ("response." + methodName).replace(".","_");
        %>
        <a name="<%= methodName %>"></a>

        <h2>
            <span><%= nsDoc.getName() + "." + methodDoc.getName() %></span>
        </h2>
        <div style="padding-left: 10px; padding-right: 10px;">
        <span>
            <%= methodDoc.getDescription() %>
        </span>

        <p>
        <table>
            <caption>Parameters<caption>
            <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Description</th>
                <th>Default</th>
            </tr>

            <% for (ApiParamDoc paramDoc : methodDoc.getParams()) { %>
            <tr>
                <td>
                    <%= escapeHtml3(paramDoc.getName()) %>
                </td>
                <td>
                    <%= ApiDocProvider.activateModelLinks(paramDoc.getType()) %>
                </td>
                <td>
                    <%= escapeHtml3(nullToEmpty(paramDoc.getDescription())) %>
                </td>
                <td>
                    <code><%= escapeHtml3(nullToEmpty(paramDoc.getDefaultValue())) %><code>
                </td>
            </tr>
            <% } %>

        </table>
        <p>
        <table>
            <caption>Returns<caption>
            <tr>
                <th>Type</th>
                <th>Description</th>
            </tr>
            <tr>
                <td><%= ApiDocProvider.activateModelLinks(methodDoc.getReturnType()) %></td>
                <td><%= escapeHtml3(nullToEmpty(methodDoc.getReturns())) %></td>
            </tr>
        </table>

        <!-- <a href="ui/<%= methodName %>">Try it!</a> -->


        <button id="<%= tryItButtonId %>" class="btn btn-primary">Try it!</button>


        <p>

        <div style="display: none;" id="<%= explorerId %>">
            <div id="<%= formContainerId %>"></div>
            <p>

            <button class="btn btn-primary" onclick="submitForm('<%= escapeEcmaScript(methodName) %>', '<%= formVar %>')">Invoke</button>
            &nbsp;
            <button class="btn btn-primary" onclick="$('#<%= tryItButtonId %>').show(); $('#<%= explorerId %>').hide(150, function(){$('#<%= formContainerId %>').empty();});">Close</button>


            <h4>Request</h4>
            <pre>
                <div class="json" id="<%= requestContainerId %>" style="max-height: 500px; overflow: auto;"></div>
            </pre>

            <h4>Result</h4>
            <pre>
                <div class="json" id="<%= responseContainerId %>" style="max-height: 500px; overflow: auto;"></div>
            </pre>
        </div>

<script>

TherapiForms = {}

$(document).ready(function(){
    $("#<%= tryItButtonId %>").click(function(){

        $("#<%= tryItButtonId %>").hide();

        var schema = "<%= escapeEcmaScript(methodDoc.getRequestSchema()) %>"
        var BrutusinForms = brutusin["json-forms"];
        var form = BrutusinForms.create(JSON.parse(schema));

        var container = document.getElementById('<%= formContainerId %>');
        form.render(container);

        TherapiForms['<%= formVar %>'] = form;



        $("#<%= explorerId %>").show(150);
    });
});

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

        logit = function(x) { if (x instanceof JsonRpcError) { logerr(x);} else { alert(JSON.stringify(x, null, 2))} };
//        logit = function(x) { if (x instanceof JsonRpcError) { logerr(x);} else { $("#result").text(JSON.stringify(x, null, 2))} };
        logerr = function(x) { alert("error:" + JSON.stringify(x, null, 2)); if (x.data && x.data.detail) {console.warn(x.data.detail);}};
        //logerr = function(x) { console.warn(x); if (x.data && x.data.detail) {console.warn(x.data.detail);}};
        rethrow = function(e) { throw e; };


        function submitForm(methodName, formName) {
            var form = TherapiForms[formName];

           // alert('<%= responseContainerId %>');
            //alert(JSON.stringify(form.getData(), null, 2));

            invokeJsonRpc(methodName, form.getData()).then(function(result) {
                var json = JSON.stringify(result, null, 2);
                //alert(json);
                //alert('<%= responseContainerId %>');
                $("#response_" + methodName.replace(/\./g,'_')).text(json);


//              $('pre code div').each(function(i, block) { hljs.highlightBlock(block); });
              $("#response_" + methodName.replace(/\./g,'_')).each(function(i, block) { hljs.highlightBlock(block); });



            }).catch(logerr);
            var copy = JSON.parse(JSON.stringify(form.getData()));
            var req = {
                jsonrpc: "2.0",
                id: "",
                method: methodName,
                params: copy,
            };

            $("#request_" + methodName.replace(/\./g,'_')).text(JSON.stringify(req, null, 2))

              //$('pre code div').each(function(i, block) {
                //hljs.highlightBlock(block);
              //});

             $("#request_" + methodName.replace(/\./g,'_')).each(function(i, block) { hljs.highlightBlock(block); });;

        }


</script>



        </div>
        <br>
        <% } %>
        <% } %>
        <br>
    </div>

    <div id="footer"></div>
</div>

</body>
</html>
