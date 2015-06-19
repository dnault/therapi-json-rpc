package com.github.dnault.bozbar.core.internal;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

public class MethodDefinition {
    private final String shortName;
    private final Method method;
    private final Object owner;
    private final ImmutableList<ParameterDefinition> params;
    private final Optional<String> namespace;

    public MethodDefinition(@Nullable String namespace, @Nullable String shortName, Method method, Object owner, List<ParameterDefinition> params) {
        this.method = method;
        this.owner = owner;
        this.params = ImmutableList.copyOf(params);
        this.namespace = Optional.ofNullable(namespace);
        this.shortName = defaultIfNull(shortName, method.getName());
    }

    public String getUnqualifiedName() {
        return method.getName();
    }

    public Method getMethod() {
        return method;
    }

    public Object getOwner() {
        return owner;
    }
}
