package com.github.dnault.therapi.core.internal;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class ParameterDefinition {
    private final String name;
    private final boolean nullable;
    private final Optional<Supplier<?>> defaultValueSupplier;
    private final TypeReference type;

    public ParameterDefinition(String name, boolean nullable, @Nullable Supplier<?> defaultValueSupplier, TypeReference type) {
        this.name = name;
        this.nullable = nullable;
        this.defaultValueSupplier = Optional.ofNullable(defaultValueSupplier);
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return nullable;
    }

    public Optional<Supplier<?>> getDefaultValueSupplier() {
        return defaultValueSupplier;
    }

    public TypeReference getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ParameterDefinition{" +
                "name='" + name + '\'' +
                ", nullable=" + nullable +
                ", defaultValue=" + (defaultValueSupplier.isPresent() ? defaultValueSupplier.get().get() : "N/A") +
                '}';
    }
}
