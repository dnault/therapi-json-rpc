package com.github.therapi.core.interceptor;

import com.github.therapi.core.MethodDefinition;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A specialized method invocation that provides access to
 * the MethodDefinition of the method being invoked.
 */
public interface MethodDefinitionInvocation extends MethodInvocation {
    MethodDefinition getMethodDefinition();

    /**
     * Returns the fully qualified name of the remotable method being invoked.
     * Intended for use by MethodInterceptors that want to know the name of the method being intercepted.
     *
     * @throws ClassCastException if the given invocation does not implement {@code MethodDefinitionInvocation}.
     */
    static String getQualifiedName(MethodInvocation invocation) {
        return ((MethodDefinitionInvocation) invocation).getMethodDefinition().getQualifiedName(".");
    }
}
