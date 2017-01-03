package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.qndhtml.Attributes.attrs;
import static com.github.therapi.apidoc.qndhtml.Attributes.clazz;
import static com.github.therapi.apidoc.qndhtml.Attributes.href;
import static com.github.therapi.apidoc.qndhtml.Attributes.id;
import static com.github.therapi.apidoc.qndhtml.Attributes.name;
import static com.github.therapi.apidoc.qndhtml.Attributes.style;
import static com.github.therapi.apidoc.qndhtml.Tag.a;
import static com.github.therapi.apidoc.qndhtml.Tag.body;
import static com.github.therapi.apidoc.qndhtml.Tag.br;
import static com.github.therapi.apidoc.qndhtml.Tag.caption;
import static com.github.therapi.apidoc.qndhtml.Tag.code;
import static com.github.therapi.apidoc.qndhtml.Tag.div;
import static com.github.therapi.apidoc.qndhtml.Tag.h2;
import static com.github.therapi.apidoc.qndhtml.Tag.head;
import static com.github.therapi.apidoc.qndhtml.Tag.html;
import static com.github.therapi.apidoc.qndhtml.Tag.inlineScript;
import static com.github.therapi.apidoc.qndhtml.Tag.input;
import static com.github.therapi.apidoc.qndhtml.Tag.li;
import static com.github.therapi.apidoc.qndhtml.Tag.meta;
import static com.github.therapi.apidoc.qndhtml.Tag.p;
import static com.github.therapi.apidoc.qndhtml.Tag.preEscapedText;
import static com.github.therapi.apidoc.qndhtml.Tag.scriptLink;
import static com.github.therapi.apidoc.qndhtml.Tag.seq;
import static com.github.therapi.apidoc.qndhtml.Tag.span;
import static com.github.therapi.apidoc.qndhtml.Tag.styleSheetLink;
import static com.github.therapi.apidoc.qndhtml.Tag.table;
import static com.github.therapi.apidoc.qndhtml.Tag.td;
import static com.github.therapi.apidoc.qndhtml.Tag.text;
import static com.github.therapi.apidoc.qndhtml.Tag.th;
import static com.github.therapi.apidoc.qndhtml.Tag.title;
import static com.github.therapi.apidoc.qndhtml.Tag.tr;
import static com.github.therapi.apidoc.qndhtml.Tag.transform;
import static com.github.therapi.apidoc.qndhtml.Tag.ul;
import static com.google.common.base.Throwables.propagate;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Matcher.quoteReplacement;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;

import com.github.therapi.apidoc.qndhtml.Tag;
import com.github.therapi.core.internal.LangHelper;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ApiDocWriter {
    private static final String RESOURCE_PREFIX = "../therapi/";

    protected static String getResourcePrefix() {
        return RESOURCE_PREFIX;
    }

    public void writeTo(List<ApiNamespaceDoc> apiNamespaceDocs, Appendable out) throws IOException {
        html(attrs("lang", "en"),
                apiDocHead(),
                body(
                        div(clazz("container"),
                                div(id("sidebar"),
                                        sidebarMenu(apiNamespaceDocs)),
                                div(id("header")),
                                apiDocContent(apiNamespaceDocs)
                        )
                )
        ).writeTo(out);
    }

    protected Tag apiDocHead() {
        return head(
                meta(attrs("charset", "UTF-8")),
                meta(attrs("http-equiv", "X-UA-Compatible").attr("content", "IE=edge")),
                meta(attrs("http-equiv", "X-UA-Compatible").attr("content", "IE=edge")),

                meta(attrs("name", "viewport").attr("content", "width=device-width, initial-scale=1")),

                styleSheetLink(getResourcePrefix() + "cssmenu/styles.css"),
                scriptLink("https://code.jquery.com/jquery-1.11.3.min.js"),
                scriptLink(getResourcePrefix() + "cssmenu/script.js"),

                styleSheetLink(getResourcePrefix() + "json-forms/css/brutusin-json-forms.min.css"),
                scriptLink(getResourcePrefix() + "json-forms/js/brutusin-json-forms.js"),

                styleSheetLink(getResourcePrefix() + "highlight/styles/github-gist.css"),
                scriptLink(getResourcePrefix() + "highlight/highlight.pack.js"),

                scriptLink(getResourcePrefix() + "datagraph/jquery-jsonrpc/0.1.1/jquery.jsonrpc.js"),
                scriptLink(getResourcePrefix() + "jakearchibald/es6-promise-3.0.2/es6-promise.min.js"),

                inlineScript("$.jsonRPC.setup({\n"
                        + "    endPoint: '../jsonrpc',\n"
                        + "});"),

                styleSheetLink(getResourcePrefix() + "apidoc.css"),

                title("API Documentation")
        );
    }

    protected Tag sidebarMenu(List<ApiNamespaceDoc> apiNamespaces) {
        return div(id("cssmenu"),
                ul(transform(apiNamespaces, ns ->
                        li(clazz("has-sub"),
                                a(href("#"),
                                        span(text(ns.getName())),
                                        ul(transform(ns.getMethods(), method ->
                                                li(
                                                        a(href("#" + ns.getName() + "." + method.getName()),
                                                                span(text(method.getName())))))))))));
    }

    protected Tag apiDocContent(List<ApiNamespaceDoc> apiNamespaces) {
        return div(id("content"),
            apiDocContentHeader(),
            transform(apiNamespaces, ns -> div(
                a(name(ns.getName())),
                transform(ns.getMethods(), method -> methodDocContent(ns, method)))),
            apiDocContentFooter());
    }


  protected Tag apiDocContentHeader() {
    return seq(
        text("Auth Token: "),
        input(id("authToken").attr("size", "64")),
        br()
    );
  }

  protected Tag apiDocContentFooter() {
    return null;
  }

    protected Tag methodDocContent(ApiNamespaceDoc ns, ApiMethodDoc method) {
        final String qualifiedMethodName = ns.getName() + "." + method.getName();

        return div(
                a(name(qualifiedMethodName)),
                h2(span(text(qualifiedMethodName))),
                div(style("padding-left: 10px; padding-right: 10px;"),
                        span(text(method.getDescription())),
                        p(),
                        parametersTable(method),
                        returnTypeTable(method),
                        preEscapedText(loadTryItResource(ns, method))),
                br()
        );
    }

    protected Tag parametersTable(ApiMethodDoc method) {
        return table(clazz("params"),
                caption(text("Parameters")),
                tr(
                        th(text("Name")),
                        th(text("Type")),
                        th(text("Description")),
                        th(text("Default"))
                ),
                transform(method.getParams(), param ->
                        tr(
                                td(text(param.getName())),
                                td(preEscapedText(ApiDocProvider.activateModelLinks(param.getType()))),
                                td(text(param.getDescription())),
                                td(code(text(param.getDefaultValue())))
                        )
                )
        );
    }

    protected Tag returnTypeTable(ApiMethodDoc method) {
        return table(clazz("params"),
                caption(text("Returns")),
                tr(
                        th(text("Type")),
                        th(text("Description"))
                ),
                tr(
                        td(preEscapedText(ApiDocProvider.activateModelLinks(method.getReturnType()))),
                        td(text(method.getReturns()))
                )
        );
    }

    /**
     * Returns a Javascript Object where the keys are the header names and the values are the header values.
     * The default behavior is to use a single header with the name returned from {@link #getAuthHeaderName()}
     * and the value from the HTML input field with id "authToken".
     */
    protected String getJsonRpcRequestHeaders() {
        return "{'" + getAuthHeaderName() + "': $('#authToken').val() }";
    }

    protected String getAuthHeaderName() {
      return "X-Auth-Token";
    }

    protected String loadTryItResource(ApiNamespaceDoc ns, ApiMethodDoc method) {
        Map<String, String> variables = new HashMap<>();

        Arrays.asList(
                "formContainerId",
                "tryItButtonId",
                "explorerId",
                "formVar")
                .forEach(variableName -> variables.put(variableName, buildName(ns, method, variableName)));

        variables.put("requestHeaders", getJsonRpcRequestHeaders());
        variables.put("methodNameJsEscaped", escapeEcmaScript(ns.getName() + "." + method.getName()));
        variables.put("requestSchema", escapeEcmaScript(method.getRequestSchema()));

        variables.put("requestContainerId", buildName(ns, method, "request"));
        variables.put("responseContainerId", buildName(ns, method, "response"));

        try (InputStream is = ApiDocWriter.class.getResourceAsStream("apidoc-try-it.html")) {
            String template = CharStreams.toString(new InputStreamReader(is, UTF_8));

            Pattern variablePattern = Pattern.compile("\\$\\{(.+?)\\}");

            return LangHelper.replace(template, variablePattern, matcher -> {
                String variableName = matcher.group(1);
                return quoteReplacement(variables.get(variableName));
            });

        } catch (IOException e) {
            throw propagate(e);
        }
    }

    protected String buildName(ApiNamespaceDoc ns, ApiMethodDoc method, String type) {
        return (type + "." + ns.getName() + "." + method.getName()).replace(".", "_");
    }
}
