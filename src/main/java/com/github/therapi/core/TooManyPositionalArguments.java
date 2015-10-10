package com.github.therapi.core;

public class TooManyPositionalArguments extends ParameterBindingException {
    private final int expected;
    private final int actual;

    public TooManyPositionalArguments(int expected, int actual) {
        super(null, "method was passed " + actual + " argument(s) but only accepts " + expected);
        this.expected = expected;
        this.actual = actual;
    }

    public int getExpected() {
        return expected;
    }

    public int getActual() {
        return actual;
    }
}
