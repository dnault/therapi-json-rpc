package com.github.therapi.core;

import static com.github.therapi.core.interceptor.MethodPredicates.methodAnnotatedWith;
import static com.github.therapi.core.interceptor.MethodPredicates.qualifiedName;
import static com.github.therapi.core.interceptor.MethodPredicates.namespace;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.therapi.core.annotation.Remotable;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class MethodInterceptorTest extends AbstractMethodRegistryTest {

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
    }

    @Remotable("test")
    private interface TestService {
        String getMagicWord();

        boolean notAnnotated();

        @TestAnnotation
        boolean annotated();
    }

    @Remotable("test2")
    @TestAnnotation
    private interface TestService2 {
        boolean notAnnotated();

        @TestAnnotation
        boolean annotated();
    }

    private static class AnnotatedTestService implements TestService2 {
        @Override
        public boolean notAnnotated() {
            return false;
        }

        @Override
        public boolean annotated() {
            return false;
        }
    }

    private static class NotAnnotatedTestService implements MethodInterceptorTest.TestService {
        @Override
        public String getMagicWord() {
            return "xyzzy";
        }

        @Override
        public boolean notAnnotated() {
            return false;
        }

        @Override
        public boolean annotated() {
            return false;
        }
    }

    @Before
    public void registerService() {
        registry.scan(new NotAnnotatedTestService());
        registry.scan(new AnnotatedTestService());
    }

    @Test
    public void filtersAreNestedInOrderOfRegistration() throws Exception {
        registry.intercept(x -> true, reverse());
        registry.intercept(x -> true, append(" characters"));
        registry.intercept(x -> true, length());
        check("test.getMagicWord", "{}", "'sretcarahc 5'");
    }

    private static MethodInterceptor reverse() {
        return invocation -> StringUtils.reverse((String) invocation.proceed());
    }

    private static MethodInterceptor append(String suffix) {
        return invocation -> invocation.proceed() + suffix;
    }

    private static MethodInterceptor length() {
        return invocation -> Integer.toString(((String) invocation.proceed()).length());
    }

    @Test
    public void canMatchMethodName() throws Exception {
        registry.intercept(qualifiedName("test.annotated"), invocation -> Boolean.TRUE);
        check("test.annotated", "{}", "true");
        check("test.notAnnotated", "{}", "false");
    }

    @Test
    public void canMatchMethodNamespace() throws Exception {
        registry.intercept(namespace("test"), invocation -> Boolean.TRUE);
        check("test.annotated", "{}", "true");
        check("test2.annotated", "{}", "false");
    }

    @Test
    public void canMatchMethodAnnotation() throws Exception {
        registry.intercept(methodAnnotatedWith(TestAnnotation.class), invocation -> Boolean.TRUE);
        check("test.annotated", "{}", "true");
        check("test.notAnnotated", "{}", "false");

        check("test2.annotated", "{}", "true");
        check("test2.notAnnotated", "{}", "false");
    }
}
