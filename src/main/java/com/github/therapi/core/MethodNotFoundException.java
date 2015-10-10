package com.github.therapi.core;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

public class MethodNotFoundException extends RuntimeException {
    private final String method;
    private final List<String> suggestions;

    private MethodNotFoundException(String method, @Nullable List<String> suggestions) {
        super("method not found: " + method);
        this.method = method;
        this.suggestions = suggestions == null ? ImmutableList.<String>of() : suggestions;
    }

    public static MethodNotFoundException forMethod(String method, List<String> suggestions) {
        return new MethodNotFoundException(method, suggestions);
    }

    public String getMethod() {
        return method;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
