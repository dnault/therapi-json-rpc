package com.github.dnault.therapi.apidoc;

import com.github.dnault.therapi.runtimejavadoc.Comment;

import javax.annotation.Nullable;

public interface CommentRenderer {
    String render(@Nullable Comment comment);
}
