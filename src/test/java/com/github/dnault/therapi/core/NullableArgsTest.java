package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Test;

import javax.annotation.Nullable;

public class NullableArgsTest extends AbstractMethodRegistryTest {
    @Remotable("")
    private interface InvalidAnnotationService {
        boolean echoNullablePrimitive(@Nullable boolean value);
    }

    private static class InvalidAnnotationServiceImpl implements InvalidAnnotationService {
        @Override
        public boolean echoNullablePrimitive(@Nullable boolean value) {
            return value;
        }
    }

    @Test(expected = InvalidAnnotationException.class)
    public void echoBooleanPrimitiveNullable() throws Exception {
        registry.scan(new InvalidAnnotationServiceImpl());
    }
}
