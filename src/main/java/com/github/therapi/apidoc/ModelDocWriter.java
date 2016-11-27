package com.github.therapi.apidoc;

import static com.github.therapi.qndhtml.Tag.body;
import static com.github.therapi.qndhtml.Tag.div;
import static com.github.therapi.qndhtml.Tag.h2;
import static com.github.therapi.qndhtml.Tag.html;
import static com.github.therapi.qndhtml.Tag.pre;
import static com.github.therapi.qndhtml.Tag.preEscapedText;
import static com.github.therapi.qndhtml.Tag.seq;
import static com.github.therapi.qndhtml.Tag.text;

import javax.annotation.Nullable;
import java.io.IOException;

import com.github.therapi.qndhtml.Tag;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.RuntimeJavadocReader;

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
        ClassJavadoc classDoc = new RuntimeJavadocReader().getDocumentation(modelClassName);

        if (classDoc == null || classDoc.getComment() == null) {
            return null;
        }

        return div(
                h2(text("Description")),
                preEscapedText(new CommentFormatter().format(classDoc.getComment())));
    }
}
