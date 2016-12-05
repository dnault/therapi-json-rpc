package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.qndhtml.Tag.body;
import static com.github.therapi.apidoc.qndhtml.Tag.div;
import static com.github.therapi.apidoc.qndhtml.Tag.h2;
import static com.github.therapi.apidoc.qndhtml.Tag.html;
import static com.github.therapi.apidoc.qndhtml.Tag.pre;
import static com.github.therapi.apidoc.qndhtml.Tag.preEscapedText;
import static com.github.therapi.apidoc.qndhtml.Tag.seq;
import static com.github.therapi.apidoc.qndhtml.Tag.text;

import javax.annotation.Nullable;
import java.io.IOException;

import com.github.therapi.apidoc.qndhtml.Tag;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

public class ModelDocWriter {

    public static void writeTo(String modelClassName, @Nullable String schema, Appendable out) throws IOException {
        html(body(
                getDescription(modelClassName),
                getSchema(schema)
        )).writeTo(out);
    }

    private static Tag getSchema(String schema) {
        return schema == null ? null : seq(
                h2(text("Schema")),
                pre(text(schema)));
    }

    private static Tag getDescription(String modelClassName) throws IOException {
        ClassJavadoc classDoc = RuntimeJavadoc.getJavadoc(modelClassName).orElse(null);

        if (classDoc == null || classDoc.getComment() == null) {
            return null;
        }

        return div(
                h2(text("Description")),
                preEscapedText(new CommentFormatter().format(classDoc.getComment())));
    }
}
