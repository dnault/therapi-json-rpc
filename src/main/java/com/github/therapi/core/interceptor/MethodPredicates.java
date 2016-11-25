package com.github.therapi.core.interceptor;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

import com.github.therapi.core.MethodDefinition;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * Factory methods for common method predicates, useful for registering method interceptors.
 *
 * @see com.github.therapi.core.MethodRegistry#intercept(Predicate, MethodInterceptor)
 */
public class MethodPredicates {
    private MethodPredicates() {
    }

    public static Predicate<MethodDefinition> any() {
        return methodDef -> true;
    }

    public static Predicate<MethodDefinition> methodAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return methodDef -> methodDef.getMethod().getAnnotation(annotationClass) != null;
    }

    public static Predicate<MethodDefinition> qualifiedName(String name) {
        return methodDef -> methodDef.getQualifiedName(".").equals(name);
    }

    public static Predicate<MethodDefinition> namespace(String namespace) {
        return methodDef -> methodDef.getNamespace().orElse("").equals(namespace);
    }
}
