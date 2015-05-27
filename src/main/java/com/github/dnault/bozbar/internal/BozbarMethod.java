package com.github.dnault.bozbar.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class BozbarMethod {
    private final Method method;

    public BozbarMethod(Method method) {
        this.method = method;
    }

    public BozbarMethod(String namespace, Object owner, Method method, Class invocationInterface) {
        this.method = method;
        for (Parameter p : method.getParameters()) {
            System.out.println(p.getName());
        }
    }
}
