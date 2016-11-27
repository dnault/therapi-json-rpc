package com.github.therapi.apidoc;

import static com.google.common.base.Throwables.propagate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * For quick & dirty HTML generation
 */
class Tag {
    protected final String name;
    protected final Map<String, String> attributes = new LinkedHashMap<>();
    private final List<Tag> children = new ArrayList<>();
    private boolean noClose = false;

    public Tag(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes.putAll(attributes);
    }

    public Tag(String name) {
        this(name, ImmutableMap.of());
    }

    public Tag content(Tag... children) {
        return content(Arrays.asList(children));
    }

    public Tag content(Iterable<Tag> children) {
        children.forEach(child -> {
            if (child != null) {
                this.children.add(child);
            }
        });
        return this;
    }

    public <T> Tag generateContent(Iterable<T> items, Function<T, Tag> tagGenerator) {
        items.forEach(item -> this.children.add(tagGenerator.apply(item)));
        return this;
    }

    public Tag attr(String name, String value) {
        attributes.put(name, value);
        return this;
    }


    public static Tag tag(String name) {
        return new Tag(name);
    }

    public static Tag html() {
        return tag("html");
    }

    public static Tag head() {
        return tag("head");
    }

    public static Tag meta() {
        return tag("meta");
    }

    public static Tag body() {
        return tag("body");
    }

    public static Tag pre() {
        return tag("pre");
    }

    public static Tag styleSheetLink(String url) {
        return tag("link").attr("rel", "stylesheet").attr("href", url);
    }

    public static Tag title(String title) {
        return tag("title").text(title);
    }

    public static Tag scriptLink(String url) {
        return new Tag("script").text("").attr("src", url);
    }

    public static Tag inlineScript(String text) {
        return new Tag("script").text("\n" + text + "\n");
    }

    public static Tag div(String id) {
        return new Tag("div").attr("id", id).text("");
    }

    public static Tag div() {
        return new Tag("div").text("");
    }

    public static Tag ul() {
        return new Tag("ul");
    }

    public static Tag li() {
        return new Tag("li");
    }

    public static Tag span() {
        return new Tag("span");
    }

    public static Tag a(String href) {
        return a().attr("href", href);
    }

    public static Tag a() {
        return new Tag("a").text("");
    }

    public static Tag h1() {
        return new Tag("h1").text("");
    }

    public static Tag h2() {
        return new Tag("h2").text("");
    }

    public static Tag h3() {
        return new Tag("h3").text("");
    }

    public static Tag h4() {
        return new Tag("h4").text("");
    }

    public static Tag p() {
        return new Tag("p").noClose();
    }

    public static Tag br() {
        return new Tag("br").noClose();
    }

    public static Tag table() {
        return new Tag("table");
    }

    public static Tag th() {
        return new Tag("th");
    }

    public static Tag td() {
        return new Tag("td");
    }

    public static Tag tr() {
        return new Tag("tr");
    }

    public static Tag code() {
        return new Tag("code");
    }

    private Tag noClose() {
        noClose = true;
        return this;
    }


    public Tag text(String text) {
        children.add(new Tag("") {
            @Override
            public void writeTo(Appendable out) throws IOException {
                if (text != null) {
                    out.append(escape(text));
                }
            }
        });
        return this;
    }

    public static Tag preEscapedText(String text) {
        return new Tag("") {
            @Override
            public void writeTo(Appendable out) throws IOException {
                out.append(text);
            }
        };
    }

    public static void main(String[] args) {
    }

    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            writeTo(sb);
            return sb.toString();
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    public void writeTo(Appendable out) throws IOException {
        out.append("<").append(name);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            out.append(" ")
                    .append(entry.getKey())
                    .append("=\"")
                    .append(escape(entry.getValue()))
                    .append("\"");
        }

        out.append(">");

        if (!children.isEmpty() && !(children.size() == 1 && children.get(0).name.equals(""))) {
            out.append("\n");
        }

        for (Tag child : children) {
            child.writeTo(out);
        }

        if (!children.isEmpty() || !noClose) {
            out.append("</").append(name).append(">");
        }
        out.append("\n");
    }

    static String escape(String text) {
        return StringEscapeUtils.escapeHtml3(text);
    }
}
