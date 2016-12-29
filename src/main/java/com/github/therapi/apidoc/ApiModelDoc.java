package com.github.therapi.apidoc;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class ApiModelDoc {
    private final String shortName;
    private final String qualifiedName;
    private final String commentHtml;
    private final String schemaHtml;
    private final ImmutableList<ApiExampleModelDoc> examples;

    public ApiModelDoc(String shortName,
                       String qualifiedName,
                       String commentHtml,
                       String schemaHtml,
                       List<ApiExampleModelDoc> examples) {
        this.shortName = shortName;
        this.qualifiedName = qualifiedName;
        this.commentHtml = commentHtml;
        this.schemaHtml = schemaHtml;
        this.examples = ImmutableList.copyOf(examples);
    }

    public String getShortName() {
        return shortName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getCommentHtml() {
        return commentHtml;
    }

    public String getSchemaHtml() {
        return schemaHtml;
    }

    public ImmutableList<ApiExampleModelDoc> getExamples() {
        return examples;
    }
}
