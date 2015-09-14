package com.github.dnault.therapi.apidoc;

import com.github.dnault.therapi.runtimejavadoc.Comment;
import com.github.dnault.therapi.runtimejavadoc.CommentElement;
import com.github.dnault.therapi.runtimejavadoc.CommentText;
import com.github.dnault.therapi.runtimejavadoc.InlineLink;
import com.github.dnault.therapi.runtimejavadoc.InlineTag;

import javax.annotation.Nullable;

public class CommentRendererImpl implements CommentRenderer {

    public String render(@Nullable Comment comment) {
        if (comment == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (CommentElement e : comment) {
            if (e instanceof CommentText) {
                sb.append(renderText((CommentText) e));

            } else if (e instanceof InlineLink) {
                sb.append(renderLink((InlineLink) e));

            } else if (e instanceof InlineTag) {
                sb.append(renderTag((InlineTag) e));

            } else {
                sb.append(renderUnrecognized(e));
            }
        }

        return sb.toString();
    }

    protected String renderText(CommentText text) {
        return text.getValue();
    }

    protected String renderTag(InlineTag e) {
        return "{@" + e.getName() + " " + e.getValue() + "}";
    }

    protected String renderLink(InlineLink e) {
        return "{@link " + e.getLink() + "}";
    }

    protected String renderUnrecognized(CommentElement e) {
        return e.toString();
    }
}
