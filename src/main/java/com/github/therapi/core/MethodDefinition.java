package com.github.therapi.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.therapi.core.annotation.Remotable;
import com.github.therapi.core.internal.JacksonHelper;
import com.github.therapi.core.internal.TypesHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public class MethodDefinition {
    private final String shortName;
    private final Method method;
    private final Method invokeVia;
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
        this.invokeVia = TypesHelper.findOnInterface(method).orElse(method);
        this.owner = owner;
        this.params = ImmutableList.copyOf(params);
        this.namespace = Optional.ofNullable(trimToNull(namespace));
        this.shortName = defaultIfNull(shortName, method.getName());
        this.requestLoggable = requestLoggable;
        this.responseLoggable = responseLoggable;
        this.returnTypeRef = JacksonHelper.getReturnTypeReference(method, owner.getClass());
        this.customAttributes = ImmutableMap.copyOf(customAttributes);
    }

    public String getUnqualifiedName() {
        return shortName;
    }

    /**
     * Returns the method bearing the {@link Remotable} annotation.
     * (If the owner is a proxy, the returned method will be the one associated with
     * the proxy target.)
     */
    public Method getMethod() {
        return method;
    }

    /**
     * If the method overrides (implements) a method of an interface,
     * returns the interface method. This is useful because if the method
     * owner is a JDK dynamic proxy created by the Spring AOP Framework,
     * we want the invoke the proxy (and not bypass the proxy by directly
     * invoking the method on the proxy target class).
     * <p>
     * If the method does not override an interface method, the returned
     * method is the same as the method returned from {@link #getMethod()}.
     */
    public Method getMethodForInvocation() {
        return invokeVia;
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

    @Override
    public String toString() {
        return getQualifiedName(".");
    }
}
