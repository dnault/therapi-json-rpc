package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.Tag.body;
import static com.github.therapi.apidoc.Tag.div;
import static com.github.therapi.apidoc.Tag.h2;
import static com.github.therapi.apidoc.Tag.html;
import static com.github.therapi.apidoc.Tag.pre;

import javax.annotation.Nullable;
import java.io.IOException;

import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.RuntimeJavadocReader;

public class ModelDocWriter {

    public static void writeTo(String modelClassName, @Nullable String schema, Appendable out) throws IOException {
        html().content(
                body().content(
                        getDescription(modelClassName),
                        getSchema(schema)
                )
        ).writeTo(out);
    }

    private static Tag getSchema(String schema) {
        return schema == null ? null : div().content(
                h2().text("Schema"),
                pre().text(schema));
    }

    private static Tag getDescription(String modelClassName) throws IOException {
        ClassJavadoc classDoc = new RuntimeJavadocReader().getDocumentation(modelClassName);

        if (classDoc == null || classDoc.getComment() == null) {
            return null;
        }

        return div().content(
                h2().text("Description")
        ).text(new CommentFormatter().format(classDoc.getComment()));
    }
}
