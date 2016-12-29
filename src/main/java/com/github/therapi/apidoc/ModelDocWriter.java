package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.qndhtml.Tag.body;
import static com.github.therapi.apidoc.qndhtml.Tag.div;
import static com.github.therapi.apidoc.qndhtml.Tag.h2;
import static com.github.therapi.apidoc.qndhtml.Tag.html;
import static com.github.therapi.apidoc.qndhtml.Tag.pre;
import static com.github.therapi.apidoc.qndhtml.Tag.preEscapedText;
import static com.github.therapi.apidoc.qndhtml.Tag.seq;
import static com.github.therapi.apidoc.qndhtml.Tag.text;
import static com.github.therapi.apidoc.qndhtml.Tag.transform;

import java.io.IOException;
import java.util.List;

import com.github.therapi.apidoc.qndhtml.Tag;

public class ModelDocWriter {

    public static void writeTo(ApiModelDoc modelDoc, Appendable out) throws IOException {
        html(body(
                getDescription(modelDoc),
                getSchema(modelDoc),
                getExamples(modelDoc)
        )).writeTo(out);
    }

    private static Tag getSchema(ApiModelDoc modelDoc) {
        String schema = modelDoc.getSchemaHtml();
        return schema == null ? null : seq(
                h2(text("Schema")),
                pre(preEscapedText(schema)));
    }

    private static Tag getDescription(ApiModelDoc modelDoc) throws IOException {
        String commentHtml = modelDoc.getCommentHtml();
        if (commentHtml == null) {
            return null;
        }

        return div(
                h2(text("Description")),
                preEscapedText(modelDoc.getCommentHtml()));
    }

    private static Tag getExamples(ApiModelDoc modelDoc) throws IOException {
        List<ApiExampleModelDoc> examples = modelDoc.getExamples();
        if (examples.isEmpty()) {
            return null;
        }
        return div(
                h2(text("Examples")),
                transform(examples, example -> pre(
                        text(example.getExampleJson()))));
    }
}
