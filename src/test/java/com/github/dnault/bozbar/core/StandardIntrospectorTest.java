package com.github.dnault.bozbar.core;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import com.github.dnault.bozbar.core.annotation.Default;
import com.github.dnault.bozbar.core.annotation.Remotable;
import com.github.dnault.bozbar.core.internal.MethodDefinition;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class StandardIntrospectorTest {
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
    public void testScan() throws Exception {
        Collection<MethodDefinition> methods = new StandardIntrospector().scan(new FooServiceImpl());

        assertEquals(ImmutableList.of("greet"),
                methods.stream().map(MethodDefinition::getUnqualifiedName).collect(toList()));
    }
}