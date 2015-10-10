package com.github.therapi.apidoc;

import com.github.therapi.runtimejavadoc.Comment;

import javax.annotation.Nullable;

public interface CommentRenderer {
    String render(@Nullable Comment comment);
}
