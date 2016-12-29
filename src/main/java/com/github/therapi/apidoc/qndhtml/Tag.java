package com.github.therapi.apidoc.qndhtml;

import static com.google.common.base.Throwables.propagate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * Quick and dirty HTML generation so we don't have to rely on Spring Boot's marginal JSP support.
 */
public class Tag {
    //CHECKSTYLE.OFF: MethodName - so we can use single-letter method names like "a" and "p"

    protected final String name;
    protected final Attributes attributes;
    protected List<Tag> content = new ArrayList<>(0);
    protected boolean noClose = false;

    public static <T> Tag transform(Collection<T> items, Function<T, Tag> tagGenerator) {
        return seq(items.stream().map(tagGenerator).toArray(Tag[]::new));
    }

    public Tag(String name) {
        this(name, new Attributes());
    }

    public Tag(String name, Attributes attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public Tag(String name, Attributes attributes, Tag... content) {
        this(name, attributes);
        add(content);
    }

    public Tag(String name, Tag... children) {
        this(name);
        add(children);
    }

    public Tag add(Tag... content) {
        for (Tag t : content) {
            if (t != null) {
                this.content.add(t);
            }
        }
        return this;
    }

    private Tag noClose() {
        noClose = true;
        return this;
    }

    public Tag attr(String name, String value) {
        attributes.attr(name, value);
        return this;
    }


    public static Tag html(Attributes attrs, Tag... content) {
        return new Tag("html", attrs, content);
    }

    public static Tag html(Tag... content) {
        return new Tag("html", content);
    }

    public static Tag meta(Attributes attrs) {
        return new Tag("meta", attrs);
    }

    public static Tag styleSheetLink(String url) {
        return new Tag("link").attr("rel", "stylesheet").attr("href", url);
    }

    public static Tag title(String title) {
        return new Tag("title", text(title));
    }

    public static Tag scriptLink(String url) {
        return new Tag("script", text("")).attr("src", url);
    }

    public static Tag inlineScript(String text) {
        return new Tag("script", preEscapedText("\n" + text + "\n"));
    }

    public static Tag body(Tag... content) {
        return new Tag("body", content);
    }

    public static Tag body(Attributes attrs, Tag... content) {
        return new Tag("body", attrs, content);
    }

    public static Tag head(Tag... content) {
        return new Tag("head", content);
    }

    public static Tag head(Attributes attrs, Tag... content) {
        return new Tag("head", attrs, content);
    }

    public static Tag pre(Tag... content) {
        return new Tag("pre", content);
    }

    public static Tag pre(Attributes attrs, Tag... content) {
        return new Tag("pre", attrs, content);
    }

    public static Tag h1(Tag... content) {
        return new Tag("h1", content);
    }

    public static Tag h1(Attributes attrs, Tag... content) {
        return new Tag("h1", attrs, content);
    }

    public static Tag h2(Tag... content) {
        return new Tag("h2", content);
    }

    public static Tag h2(Attributes attrs, Tag... content) {
        return new Tag("h2", attrs, content);
    }


    public static Tag div(Attributes attrs, Tag... content) {
        return new Tag("div", attrs, content);
    }

    public static Tag div(Tag... content) {
        return new Tag("div", content);
    }

    public static Tag ul(Attributes attrs, Tag... content) {
        return new Tag("ul", attrs, content);
    }

    public static Tag ul(Tag... content) {
        return new Tag("ul", content);
    }

    public static Tag li(Attributes attrs, Tag... content) {
        return new Tag("li", attrs, content);
    }

    public static Tag li(Tag... content) {
        return new Tag("li", content);
    }

    public static Tag a(Attributes attrs, Tag... content) {
        return new Tag("a", attrs, content);
    }

    public static Tag a(Tag... content) {
        return new Tag("a", content);
    }

    public static Tag span(Attributes attrs, Tag... content) {
        return new Tag("span", attrs, content);
    }

    public static Tag span(Tag... content) {
        return new Tag("span", content);
    }

    public static Tag p(Attributes attrs, Tag... content) {
        return new Tag("p", attrs, content).noClose();
    }

    public static Tag p(Tag... content) {
        return new Tag("p", content).noClose();
    }

    public static Tag br(Attributes attrs, Tag... content) {
        return new Tag("br", attrs, content).noClose();
    }

    public static Tag br(Tag... content) {
        return new Tag("br", content).noClose();
    }


    public static Tag table(Attributes attrs, Tag... content) {
        return new Tag("table", attrs, content);
    }

    public static Tag table(Tag... content) {
        return new Tag("table", content);
    }

    public static Tag tr(Attributes attrs, Tag... content) {
        return new Tag("tr", attrs, content);
    }

    public static Tag tr(Tag... content) {
        return new Tag("tr", content);
    }

    public static Tag td(Attributes attrs, Tag... content) {
        return new Tag("td", attrs, content);
    }

    public static Tag td(Tag... content) {
        return new Tag("td", content);
    }

    public static Tag th(Attributes attrs, Tag... content) {
        return new Tag("th", attrs, content);
    }

    public static Tag th(Tag... content) {
        return new Tag("th", content);
    }

    public static Tag caption(Attributes attrs, Tag... content) {
        return new Tag("caption", attrs, content);
    }

    public static Tag caption(Tag... content) {
        return new Tag("caption", content);
    }

    public static Tag code(Attributes attrs, Tag... content) {
        return new Tag("code", attrs, content);
    }

    public static Tag code(Tag... content) {
        return new Tag("code", content);
    }

    public static Tag input(Attributes attrs, Tag... content) {
        return new Tag("input", attrs, content).noClose();
    }

    public static Tag input(Tag... content) {
        return new Tag("input", content).noClose();
    }

    public static Tag text(String text) {
        return new Tag("") {
            @Override
            public void writeTo(Appendable out) throws IOException {
                if (text != null) {
                    out.append(escape(text));
                }
            }
        };
    }

    public static Tag preEscapedText(String text) {
        return new Tag("") {
            @Override
            public void writeTo(Appendable out) throws IOException {
                out.append(text);
            }
        };
    }

    /**
     * Returns a pseudo-tag allowing multiple tags to be created as a single unit
     */
    public static Tag seq(Tag... tags) {
        return new Tag("seq", tags) {
            @Override
            public void writeTo(Appendable out) throws IOException {
                for (Tag t : this.content) {
                    t.writeTo(out);
                }
            }
        };
    }

    public void writeTo(Appendable out) throws IOException {
        out.append("<").append(name);
        attributes.writeTo(out);
        out.append(">");

        if (!content.isEmpty() && !(content.size() == 1 && content.get(0).name.equals(""))) {
            out.append("\n");
        }

        for (Tag child : content) {
            child.writeTo(out);
        }

        if (!content.isEmpty() || !noClose) {
            out.append("</").append(name).append(">");
        }
        out.append("\n");
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

    static String escape(String text) {
        return StringEscapeUtils.escapeHtml3(text);
    }
}
