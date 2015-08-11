package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.annotation.Remotable;
import com.github.dnault.therapi.core.internal.JacksonHelper;
import org.junit.Test;

public class MethodRegistryTest {

    @Remotable("foo")
    public interface FooService {
        String greet(@Default("stranger") String name);
    }

    public static class FooServiceImpl implements FooService {
        @Override
        public String greet(String name) {
            return "Hello " + name;
        }
    }

    @Test
    public void foo() throws Exception {
        MethodRegistry context = new MethodRegistry(JacksonHelper.newLenientObjectMapper());
        context.scan(new FooServiceImpl());

        System.out.println(context.invoke("greet", context.getObjectMapper().readTree("{name:'henry'}")));
        System.out.println(context.invoke("greet", context.getObjectMapper().readTree("[]")));
    }
}
