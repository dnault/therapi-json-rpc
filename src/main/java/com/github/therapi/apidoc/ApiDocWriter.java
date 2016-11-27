package com.github.therapi.apidoc;

import static com.github.therapi.qndhtml.Attributes.attrs;
import static com.github.therapi.qndhtml.Attributes.clazz;
import static com.github.therapi.qndhtml.Attributes.href;
import static com.github.therapi.qndhtml.Attributes.id;
import static com.github.therapi.qndhtml.Attributes.name;
import static com.github.therapi.qndhtml.Attributes.style;
import static com.github.therapi.qndhtml.Tag.a;
import static com.github.therapi.qndhtml.Tag.body;
import static com.github.therapi.qndhtml.Tag.br;
import static com.github.therapi.qndhtml.Tag.caption;
import static com.github.therapi.qndhtml.Tag.code;
import static com.github.therapi.qndhtml.Tag.div;
import static com.github.therapi.qndhtml.Tag.h2;
import static com.github.therapi.qndhtml.Tag.head;
import static com.github.therapi.qndhtml.Tag.html;
import static com.github.therapi.qndhtml.Tag.inlineScript;
import static com.github.therapi.qndhtml.Tag.li;
import static com.github.therapi.qndhtml.Tag.meta;
import static com.github.therapi.qndhtml.Tag.p;
import static com.github.therapi.qndhtml.Tag.preEscapedText;
import static com.github.therapi.qndhtml.Tag.scriptLink;
import static com.github.therapi.qndhtml.Tag.span;
import static com.github.therapi.qndhtml.Tag.styleSheetLink;
import static com.github.therapi.qndhtml.Tag.table;
import static com.github.therapi.qndhtml.Tag.td;
import static com.github.therapi.qndhtml.Tag.text;
import static com.github.therapi.qndhtml.Tag.th;
import static com.github.therapi.qndhtml.Tag.title;
import static com.github.therapi.qndhtml.Tag.tr;
import static com.github.therapi.qndhtml.Tag.transform;
import static com.github.therapi.qndhtml.Tag.ul;
import static com.google.common.base.Throwables.propagate;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.therapi.qndhtml.Tag;
import com.google.common.io.CharStreams;

public class ApiDocWriter {
    private static final String RESOURCE_PREFIX = "../therapi/";

    public static void writeTo(List<ApiNamespaceDoc> apiNamespaceDocs, Appendable out) throws IOException {
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

    private static Tag apiDocHead() {
        return head(
                meta(attrs("charset", "UTF-8")),
                meta(attrs("http-equiv", "X-UA-Compatible").attr("content", "IE=edge")),
                meta(attrs("http-equiv", "X-UA-Compatible").attr("content", "IE=edge")),

                meta(attrs("name", "viewport").attr("content", "width=device-width, initial-scale=1")),

                styleSheetLink(RESOURCE_PREFIX + "cssmenu/styles.css"),
                scriptLink("https://code.jquery.com/jquery-1.11.3.min.js"),
                scriptLink(RESOURCE_PREFIX + "cssmenu/script.js"),

                styleSheetLink(RESOURCE_PREFIX + "json-forms/css/brutusin-json-forms.min.css"),
                scriptLink(RESOURCE_PREFIX + "json-forms/js/brutusin-json-forms.min.js"),

                styleSheetLink(RESOURCE_PREFIX + "highlight/styles/github-gist.css"),
                scriptLink(RESOURCE_PREFIX + "highlight/highlight.pack.js"),

                scriptLink(RESOURCE_PREFIX + "datagraph/jquery-jsonrpc/0.1.1/jquery.jsonrpc.js"),
                scriptLink(RESOURCE_PREFIX + "jakearchibald/es6-promise-3.0.2/es6-promise.min.js"),

                inlineScript("$.jsonRPC.setup({\n"
                        + "    endPoint: '../jsonrpc',\n"
                        + "});"),

                styleSheetLink(RESOURCE_PREFIX + "apidoc.css"),

                title("API Documentation")
        );
    }

    private static Tag sidebarMenu(List<ApiNamespaceDoc> apiNamespaces) {
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

    private static Tag apiDocContent(List<ApiNamespaceDoc> apiNamespaces) {
        return div(id("content"),
                transform(apiNamespaces, ns -> div(
                        a(name(ns.getName())),
                        transform(ns.getMethods(), method -> methodDocContent(ns, method)))));
    }

    private static Tag methodDocContent(ApiNamespaceDoc ns, ApiMethodDoc method) {
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

    private static Tag parametersTable(ApiMethodDoc method) {
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

    private static Tag returnTypeTable(ApiMethodDoc method) {
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

    private static String loadTryItResource(ApiNamespaceDoc ns, ApiMethodDoc method) {
        Map<String, String> variables = new HashMap<>();

        Arrays.asList(
                "formContainerId",
                "tryItButtonId",
                "explorerId",
                "formVar")
                .forEach(variableName -> variables.put(variableName, buildName(ns, method, variableName)));

        variables.put("methodNameJsEscaped", escapeEcmaScript(ns.getName() + "." + method.getName()));
        variables.put("requestSchema", escapeEcmaScript(method.getRequestSchema()));

        variables.put("requestContainerId", buildName(ns, method, "request"));
        variables.put("responseContainerId", buildName(ns, method, "response"));

        try (InputStream is = ApiDocWriter.class.getResourceAsStream("apidoc-try-it.html")) {
            String template = CharStreams.toString(new InputStreamReader(is, UTF_8));

            Pattern variablePattern = Pattern.compile("\\$\\{(.+?)\\}");

            StringBuffer sb = new StringBuffer();
            Matcher m = variablePattern.matcher(template);
            while (m.find()) {
                String variableName = m.group(1);
                String replacement = variables.get(variableName);
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            return sb.toString();

        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private static String buildName(ApiNamespaceDoc ns, ApiMethodDoc method, String type) {
        return (type + "." + ns.getName() + "." + method.getName()).replace(".", "_");
    }
}
