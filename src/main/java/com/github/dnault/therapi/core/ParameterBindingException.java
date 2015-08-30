package com.github.dnault.therapi.core;

import java.util.Optional;

public class ParameterBindingException extends RuntimeException {
    private final Optional<String> parameterName;

    public ParameterBindingException(String parameterName, String message) {
        super(message);
        this.parameterName = Optional.ofNullable(parameterName);
    }

    public Optional<String> getParameterName() {
        return parameterName;
    }
}
