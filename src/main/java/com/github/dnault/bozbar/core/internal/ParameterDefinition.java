package com.github.dnault.bozbar.core.internal;
//package com.mobileiron.highnode.core.internal;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class ParameterDefinition {
    private final String name;
    private final boolean nullable;
    private final Optional<Supplier<?>> defaultValueSupplier;
    private final Class<?> type;

    public ParameterDefinition(String name, boolean nullable, @Nullable Supplier<?> defaultValueSupplier, Class<?> type) {
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

    public Class<?> getType() {
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
