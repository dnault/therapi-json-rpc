package com.github.dnault.bozbar.core.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodDefinition {
    private final Method method;
    private final Object owner;

    public MethodDefinition(Method method, Object owner) {
        this.method = method;
        this.owner = owner;
    }

    public MethodDefinition(String namespace, Object owner, Method method, Class<?> invocationInterface) {
        this(method, owner);
        for (Parameter p : method.getParameters()) {
            System.out.println(p.getName());
        }
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
