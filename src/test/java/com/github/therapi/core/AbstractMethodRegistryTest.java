package com.github.therapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.reflect.AbstractInvocationHandler;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.Before;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.github.therapi.core.internal.JacksonHelper.isLikeNull;
import static com.github.therapi.core.internal.JacksonHelper.newLenientObjectMapper;
import static com.google.common.base.Throwables.propagate;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.junit.Assert.fail;

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

    protected <T extends Throwable> T check(String methodName, String args, Class<T> expectedError) throws IOException {
        try {
            registry.invoke(methodName, readTree(args));
            fail("expected exception " + expectedError + " was not thrown");

        } catch (Throwable t) {
            if (!expectedError.isAssignableFrom(t.getClass())) {
                throw propagate(t);
            }
            return expectedError.cast(t);
        }

        throw new Error("unreachable code");
    }

    protected JsonNode readTree(String json) throws IOException {
        return registry.getObjectMapper().readTree(json);
    }

    /**
     * Creates a dynamic proxy implementing the given interface.
     * All of the proxy's one-arg methods will return their argument.
     * All other methods return a List containing their arguments.
     */
    protected <T> T newEchoProxyInstance(Class<T> clazz) {
        return clazz.cast(newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            @ParametersAreNonnullByDefault
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                return args.length == 0 ? null : args.length == 1 ? args[0] : Arrays.asList(args);
            }
        }));
    }
}
