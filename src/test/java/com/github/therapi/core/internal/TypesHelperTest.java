package com.github.therapi.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.Test;

public class TypesHelperTest {

    @SuppressWarnings("unused")
    private interface SomeInterface {
        void someMethod();

        default String someMethod(String s) {
            return s;
        }
    }

    private interface SomeMiddleInterface extends SomeInterface {}

    @SuppressWarnings("unused")
    private static class SomeClass implements SomeMiddleInterface {
        @Override
        public void someMethod() {
        }

        @Override
        public String someMethod(String s) {
            return null;
        }

        void someOtherMethod() {
        }
    }

    @Test
    public void findOnInterface() throws Exception {
        Method classMethod = SomeClass.class.getDeclaredMethod("someMethod", String.class);
        assertNotNull(classMethod);

        Method ifaceMethod = TypesHelper.findOnInterface(classMethod).orElse(null);

        assertEquals(SomeInterface.class.getDeclaredMethod("someMethod", String.class), ifaceMethod);
        assertNotEquals(SomeInterface.class.getDeclaredMethod("someMethod", String.class), classMethod);

        assertEquals(Optional.empty(), TypesHelper.findOnInterface(
                SomeClass.class.getDeclaredMethod("someOtherMethod")));
    }

    @Test
    public void findOnInterfaceWorksWithMethodFromInterface() throws Exception {
        Method m = SomeInterface.class.getDeclaredMethod("someMethod", String.class);
        assertNotNull(m);

        Method ifaceMethod = TypesHelper.findOnInterface(m).orElse(null);

        assertEquals(m, ifaceMethod);
    }
}