package com.github.therapi.apidoc;

public class ApiExampleModelDoc {
    private final String commentHtml;
    private final String exampleJson;

    public ApiExampleModelDoc(String commentHtml, String exampleJson) {
        this.commentHtml = commentHtml;
        this.exampleJson = exampleJson;
    }

    public String getCommentHtml() {
        return commentHtml;
    }

    public String getExampleJson() {
        return exampleJson;
    }
}
