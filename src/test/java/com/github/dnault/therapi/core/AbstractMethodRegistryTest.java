package com.github.dnault.therapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.reflect.AbstractInvocationHandler;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.Before;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.reflect.Method;

import static com.github.dnault.therapi.core.internal.JacksonHelper.isLikeNull;
import static com.github.dnault.therapi.core.internal.JacksonHelper.newLenientObjectMapper;
import static java.lang.reflect.Proxy.newProxyInstance;

public class AbstractMethodRegistryTest {
    protected MethodRegistry registry;

    @Before
    public final void createMethodRegistry() {
        registry = new MethodRegistry(newLenientObjectMapper());
    }

    protected void check(String methodName, String args, String expectedResult) throws IOException {
        JsonNode result = registry.invoke(methodName, readTree(args));
        JsonNode expected = readTree(expectedResult);

        if (isLikeNull(expected)) {
            expected = null;
        }

        JsonAssert.assertJsonEquals(expected, result);
    }

    protected JsonNode readTree(String json) throws IOException {
        return registry.getObjectMapper().readTree(json);
    }

    /**
     * Creates a dynamic proxy implementing the given interface.
     * All of the proxy's methods will return their first argument.
     */
    protected <T> T newEchoProxyInstance(Class<T> clazz) {
        return clazz.cast(newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            @ParametersAreNonnullByDefault
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                return args[0];
            }
        }));
    }
}
