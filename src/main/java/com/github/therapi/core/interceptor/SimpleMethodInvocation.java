package com.github.therapi.core.interceptor;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.github.therapi.core.MethodDefinition;
import com.google.common.collect.ImmutableList;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * No-frills implementation of the AOP Alliance MethodInvocation interface.
 * Supports only static matching. If dynamic argument matching or other advanced
 * features are required, consider subclassing Spring's ReflectiveMethodInvocation
 * class instead.
 */
public class SimpleMethodInvocation implements MethodDefinitionInvocation {
    private final MethodDefinition methodDefinition;
    private final Object[] arguments;
    private final ImmutableList<MethodInterceptor> interceptors;
    private int currentInterceptorIndex = -1;

    public SimpleMethodInvocation(MethodDefinition methodDefinition, Object[] args, List<MethodInterceptor> interceptors) {
        this.methodDefinition = requireNonNull(methodDefinition);
        this.arguments = requireNonNull(args);
        this.interceptors = ImmutableList.copyOf(interceptors);
    }

    @Override
    public MethodDefinition getMethodDefinition() {
        return methodDefinition;
    }

    @Override
    public Method getMethod() {
        return methodDefinition.getMethodForInvocation();
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {
        return currentInterceptorIndex == interceptors.size() - 1
                ? invokeTargetMethod()
                : interceptors.get(++currentInterceptorIndex).invoke(this);
    }

    protected Object invokeTargetMethod() throws Throwable {
        Method method = getMethod();

        try {
            return method.invoke(methodDefinition.getOwner(), arguments);

        } catch (IllegalAccessException e) {
            method.setAccessible(true);
            return method.invoke(methodDefinition.getOwner(), arguments);

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @Override
    public Object getThis() {
        return methodDefinition.getOwner();
    }

    @Override
    public AccessibleObject getStaticPart() {
        return getMethod();
    }
}
