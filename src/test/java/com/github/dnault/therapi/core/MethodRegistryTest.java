package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

public class MethodRegistryTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(new FooServiceImpl());
    }

    @Remotable("")
    interface FooService {
        String greet(@Default("stranger") String name);
    }

    private static class FooServiceImpl implements FooService {
        @Override
        public String greet(String name) {
            return "Hello " + name;
        }
    }

    @Test
    public void foo() throws Exception {
        check("greet", "{name:'henry'}", "'Hello henry'");
        check("greet", "[]", "'Hello stranger'");
    }
}
