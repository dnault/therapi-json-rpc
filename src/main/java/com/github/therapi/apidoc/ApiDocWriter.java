package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.Tag.a;
import static com.github.therapi.apidoc.Tag.body;
import static com.github.therapi.apidoc.Tag.br;
import static com.github.therapi.apidoc.Tag.code;
import static com.github.therapi.apidoc.Tag.div;
import static com.github.therapi.apidoc.Tag.h2;
import static com.github.therapi.apidoc.Tag.head;
import static com.github.therapi.apidoc.Tag.html;
import static com.github.therapi.apidoc.Tag.inlineScript;
import static com.github.therapi.apidoc.Tag.li;
import static com.github.therapi.apidoc.Tag.meta;
import static com.github.therapi.apidoc.Tag.p;
import static com.github.therapi.apidoc.Tag.preEscapedText;
import static com.github.therapi.apidoc.Tag.scriptLink;
import static com.github.therapi.apidoc.Tag.span;
import static com.github.therapi.apidoc.Tag.styleSheetLink;
import static com.github.therapi.apidoc.Tag.table;
import static com.github.therapi.apidoc.Tag.tag;
import static com.github.therapi.apidoc.Tag.td;
import static com.github.therapi.apidoc.Tag.th;
import static com.github.therapi.apidoc.Tag.title;
import static com.github.therapi.apidoc.Tag.tr;
import static com.github.therapi.apidoc.Tag.ul;
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

import com.google.common.io.CharStreams;

public class ApiDocWriter {
    private static final String RESOURCE_PREFIX = "../therapi/";

    public static void writeTo(List<ApiNamespaceDoc> apiNamespaceDocs, Appendable out) throws IOException {
        html().attr("lang", "en").content(
                apiDocHead(),
                body().content(
                        div().attr("class", "container").content(
                                div("sidebar").content(sidebarMenu(apiNamespaceDocs)),
                                div("header"),
                                apiDocContent(apiNamespaceDocs)
                        )
                )
        ).writeTo(out);
    }

    private static Tag apiDocHead() {
        return head().content(
                meta().attr("charset", "UTF-8"),
                meta().attr("http-equiv", "X-UA-Compatible").attr("content", "IE=edge"),
                meta().attr("http-equiv", "X-UA-Compatible").attr("content", "IE=edge"),

                meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1"),

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
        return div("cssmenu").content(
                ul().generateContent(apiNamespaces, ns ->
                        li().attr("class", "has-sub").content(
                                a("#").content(span().text(ns.getName())),
                                ul().generateContent(ns.getMethods(), method ->
                                        li().content(
                                                a("#" + ns.getName() + "." + method.getName()).content(
                                                        span().text(method.getName())))))));
    }

    private static Tag apiDocContent(List<ApiNamespaceDoc> apiNamespaces) {
        return div("content").generateContent(apiNamespaces, ns ->
                div().content(
                        a().attr("name", ns.getName())
                ).generateContent(ns.getMethods(), method -> methodDocContent(ns, method)));
    }

    private static String name(ApiNamespaceDoc ns, ApiMethodDoc method, String type) {
        return (type + "." + ns.getName() + "." + method.getName()).replace(".", "_");
    }

    private static Tag methodDocContent(ApiNamespaceDoc ns, ApiMethodDoc method) {
        final String qualifiedMethodName = ns.getName() + "." + method.getName();

        return div().content(
                a().attr("name", qualifiedMethodName),
                h2().content(span().text(qualifiedMethodName)),
                div().attr("style", "padding-left: 10px; padding-right: 10px;").content(
                        span().text(method.getDescription()),
                        p(),
                        parametersTable(method),
                        returnTypeTable(method),
                        preEscapedText(loadTryItResource(ns, method))),
                br()
        );
    }

    private static Tag parametersTable(ApiMethodDoc method) {
        return table().attr("class", "params").content(
                tag("caption").text("Parameters"),
                tr().content(
                        th().text("Name"),
                        th().text("Type"),
                        th().text("Description"),
                        th().text("Default")
                )).generateContent(method.getParams(), param ->
                tr().content(
                        td().text(param.getName()),
                        td().content(preEscapedText(ApiDocProvider.activateModelLinks(param.getType()))),
                        td().text(param.getDescription()),
                        td().content(
                                code().text(param.getDefaultValue()))
                )
        );
    }

    private static Tag returnTypeTable(ApiMethodDoc method) {
        return table().attr("class", "params").content(
                tag("caption").text("Returns"),
                tr().content(
                        th().text("Type"),
                        th().text("Description")
                ),
                tr().content(
                        td().content(preEscapedText(ApiDocProvider.activateModelLinks(method.getReturnType()))),
                        td().text(method.getReturns())
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
                .forEach(variableName -> variables.put(variableName, name(ns, method, variableName)));

        variables.put("methodNameJsEscaped", escapeEcmaScript(ns.getName() + "." + method.getName()));
        variables.put("requestSchema", escapeEcmaScript(method.getRequestSchema()));

        variables.put("requestContainerId", name(ns, method, "request"));
        variables.put("responseContainerId", name(ns, method, "response"));

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
}
