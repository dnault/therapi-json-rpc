package com.github.therapi.core;

import static com.google.common.base.Strings.emptyToNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.therapi.core.internal.JacksonHelper;
import com.google.common.collect.ImmutableList;

public class MethodDefinition {
    private final String shortName;
    private final Method method;
    private final Object owner;
    private final ImmutableList<ParameterDefinition> params;
    private final Optional<String> namespace;
    private final TypeReference returnTypeRef;

    public MethodDefinition(@Nullable String namespace, @Nullable String shortName, Method method, Object owner, List<ParameterDefinition> params) {
        this.method = method;
        this.owner = owner;
        this.params = ImmutableList.copyOf(params);
        this.namespace = Optional.ofNullable(emptyToNull(namespace));
        this.shortName = defaultIfNull(shortName, method.getName());
        returnTypeRef = JacksonHelper.getReturnTypeReference(method, owner.getClass());
    }

    public String getUnqualifiedName() {
        return shortName;
    }

    public Method getMethod() {
        return method;
    }

    public Object getOwner() {
        return owner;
    }

    public ImmutableList<ParameterDefinition> getParameters() {
        return params;
    }

    public String getQualifiedName(String namespaceSeparator) {
        return namespace.isPresent() ? (namespace.get() + namespaceSeparator + shortName) : shortName;
    }

    public TypeReference getReturnTypeRef() {
        return returnTypeRef;
    }

    public Optional<String> getNamespace() {
        return namespace;
    }
}
