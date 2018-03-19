package com.github.therapi.apidoc.qndhtml;

import static com.github.therapi.apidoc.qndhtml.Tag.escape;
import static com.github.therapi.core.internal.LangHelper.propagate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


public class Attributes {
    private final Map<String, String> map = new LinkedHashMap<>(0);

    public static Attributes attrs(String name, String value) {
        return new Attributes().attr(name, value);
    }

    public static Attributes attrs() {
        return new Attributes();
    }

    public static Attributes clazz(String value) {
        return attrs("class", value);
    }

    public static Attributes src(String value) {
        return attrs("src", value);
    }

    public static Attributes href(String value) {
        return attrs("href", value);
    }

    public static Attributes name(String value) {
        return attrs("name", value);
    }

    public static Attributes id(String value) {
        return attrs("id", value);
    }

    public static Attributes style(String value) {
        return attrs("style", value);
    }

    public Attributes attr(String name, String value) {
        map.put(name, value);
        return this;
    }

    public void writeTo(Appendable out) throws IOException {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.append(" ")
                    .append(entry.getKey())
                    .append("=\"")
                    .append(escape(entry.getValue()))
                    .append("\"");
        }
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
}
