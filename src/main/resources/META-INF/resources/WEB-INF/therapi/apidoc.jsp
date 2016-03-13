<%@ page import="com.github.therapi.apidoc.TherapiMethodDoc" %>
<%@ page import="com.github.therapi.apidoc.TherapiNamespaceDoc" %>
<%@ page import="com.github.therapi.apidoc.TherapiParamDoc" %>
<%@ page import="static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3" %>
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
    <title>API Documentation</title>
</head>
<body>
<style>
    .container {
        position: relative;
        padding: 0 0 0 55px;
    }

    #sidebar {
        position: fixed;
        left: 10px;
        top: 10px; /* change to adjust height from the top of the page */
    }

    #content {
        margin-left: 250px;
    }

    table {
        border-collapse: collapse;
        width: 100%;
        margin: 0 0 20px 0;
    }

    th {
        background-color: #f5f5f5;
        text-align: left;
        font-family: "Source Sans Pro", sans-serif;
        font-weight: 700;
        padding: 4px 8px;
        border: #e0e0e0 1px solid;
    }

    td {
        vertical-align: top;
        padding: 2px 8px;
        border: #e0e0e0 1px solid;
    }


</style>

<div class="container">
    <div id="sidebar">
        <div id="cssmenu">
            <ul>
                <!-- <li class="active"><a href="#"><span>Home</span></a></li> -->

                <% for (TherapiNamespaceDoc nsDoc : (List<TherapiNamespaceDoc>) request.getAttribute("therapiNamespaces")) { %>
                <li class="has-sub"><a href="#"><span><%= nsDoc.getName() %></span></a>
                    <ul>
                        <% for (TherapiMethodDoc methodDoc : nsDoc.getMethods()) { %>
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

        <% for (TherapiNamespaceDoc nsDoc : (List<TherapiNamespaceDoc>) request.getAttribute("therapiNamespaces")) { %>
        <a name="<%=nsDoc.getName()%>"></a>

        <%--
        <h1>
            <%=nsDoc.getName()%>
        </h1>
        --%>

        <% for (TherapiMethodDoc methodDoc : nsDoc.getMethods()) { %>
        <a name="<%=nsDoc.getName() + "." + methodDoc.getName()%>"></a>

        <h2>
            <span><%= nsDoc.getName() + "." + methodDoc.getName() %></span>
        </h2>

        <span>
            <%= methodDoc.getDescription() %>
        </span>

        <p>
        <table>
            <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Description</th>
                <th>Default</th>
            </tr>

            <% for (TherapiParamDoc paramDoc : methodDoc.getParams()) { %>
            <tr>
                <td>
                    <%= escapeHtml3(paramDoc.getName()) %>
                </td>
                <td>
                    <%= escapeHtml3(paramDoc.getType()) %>
                </td>
                <td>
                    <%= escapeHtml3(nullToEmpty(paramDoc.getDescription())) %>
                </td>
                <td>
                    <%= escapeHtml3(nullToEmpty(paramDoc.getDefaultValue())) %>
                </td>
            </tr>
            <% } %>

        </table>
        <% } %>
        <% } %>
        <br>
    </div>

    <div id="footer"></div>
</div>

</body>
</html>
