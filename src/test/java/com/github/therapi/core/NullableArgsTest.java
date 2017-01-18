package com.github.therapi.core;

import com.github.therapi.core.annotation.Remotable;
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

    @Remotable("")
    @SuppressWarnings("unused")
    private interface NullableEchoService {
        Integer echoNullableInteger(@Nullable Integer value);
    }

    @Test
    public void echoNullableInteger() throws Exception {
        registry.scan(newEchoProxyInstance(NullableEchoService.class));
        check("echoNullableInteger", "{}", "null");
        check("echoNullableInteger", "[null]", "null");
        check("echoNullableInteger", "{value:null}", "null");
    }
}
