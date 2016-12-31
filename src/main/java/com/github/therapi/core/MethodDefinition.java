package com.github.therapi.core;

import static com.google.common.base.Strings.emptyToNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
    private final boolean requestLoggable;
    private final boolean responseLoggable;
    private final ImmutableMap<String, Object> customAttributes;

    public MethodDefinition(@Nullable String namespace, @Nullable String shortName, Method method, Object owner,
            List<ParameterDefinition> params,
            boolean requestLoggable, boolean responseLoggable,
            Map<String, Object> customAttributes) {
        this.method = method;
        this.owner = owner;
        this.params = ImmutableList.copyOf(params);
        this.namespace = Optional.ofNullable(emptyToNull(namespace));
        this.shortName = defaultIfNull(shortName, method.getName());
        this.requestLoggable = requestLoggable;
        this.responseLoggable = responseLoggable;
        this.returnTypeRef = JacksonHelper.getReturnTypeReference(method, owner.getClass());
        this.customAttributes = ImmutableMap.copyOf(customAttributes);
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

    public boolean isRequestLoggable() {
        return requestLoggable;
    }

    public boolean isResponseLoggable() {
        return responseLoggable;
    }

    public ImmutableMap<String, Object> getCustomAttributes() {
        return customAttributes;
    }
}
