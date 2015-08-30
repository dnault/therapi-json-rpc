package com.github.dnault.therapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;

import java.io.IOException;

import static com.github.dnault.therapi.core.internal.JacksonHelper.isLikeNull;
import static com.github.dnault.therapi.core.internal.JacksonHelper.newLenientObjectMapper;
import static org.junit.Assert.assertEquals;

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

        assertEquals(expected, result);
    }

    protected JsonNode readTree(String json) throws IOException {
        return registry.getObjectMapper().readTree(json);
    }
}
