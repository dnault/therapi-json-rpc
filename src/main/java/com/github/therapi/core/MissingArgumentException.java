package com.github.therapi.core;

public class MissingArgumentException extends ParameterBindingException {
    public MissingArgumentException(String parameterName) {
        super(parameterName, "missing '" + parameterName + "' parameter");
    }
}
