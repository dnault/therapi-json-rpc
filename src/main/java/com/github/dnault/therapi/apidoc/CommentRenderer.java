package com.github.dnault.therapi.apidoc;

import com.github.dnault.therapi.runtimejavadoc.Comment;

public interface CommentRenderer {
    String render(Comment comment);
}
