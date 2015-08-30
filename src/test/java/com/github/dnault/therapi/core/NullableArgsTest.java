package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Test;

import javax.annotation.Nullable;

public class NullableArgsTest extends AbstractMethodRegistryTest {
    @Remotable("")
    @SuppressWarnings("unused")
    private interface InvalidAnnotationService {
        boolean echoNullablePrimitive(@Nullable boolean value);
    }

    @Test(expected = InvalidAnnotationException.class)
    public void echoBooleanPrimitiveNullable() throws Exception {
        registry.scan(newEchoProxyInstance(InvalidAnnotationService.class));
    }
}
