package com.github.dnault.therapi.core;

public class NullArgumentException extends ParameterBindingException {
    public NullArgumentException(String parameterName) {
        super(parameterName, "value for parameter '" + parameterName + "' must be non-null");
    }
}
