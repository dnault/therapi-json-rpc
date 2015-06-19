package com.github.dnault.bozbar.core.internal;

import java.util.Optional;
import java.util.function.Supplier;

public class ParameterDefinition {
    private final String name;
    private final boolean nullable;
    private final Optional<Supplier<?>> defaultValueSupplier;

    public ParameterDefinition(String name) {
        this.name = name;
        this.nullable = true;
        this.defaultValueSupplier = null;
    }
}
