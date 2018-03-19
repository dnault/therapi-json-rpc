package com.github.therapi.core;

@FunctionalInterface
public interface ProxyIntrospector {
    /**
     * If the given object is a proxy, returns the class of the innermost
     * proxy target. Otherwise returns the given object's class.
     * Useful because annotations might not be visible on the proxy.
     */
    Class<?> getProxyTargetClass(Object object);
}
