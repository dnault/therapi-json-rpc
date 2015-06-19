package com.github.dnault.bozbar.core;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.ClassUtils.getAllInterfaces;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import com.github.dnault.bozbar.core.annotation.Remotable;
import com.github.dnault.bozbar.core.internal.MethodDefinition;

public class StandardIntrospector implements Introspector {
    @Override
    public Collection<MethodDefinition> scan(Object o) {
        return Arrays.stream(getAllInterfaces(o))
                .filter(iface -> iface.isAnnotationPresent(Remotable.class))
                .flatMap(iface -> scan(o, iface, iface.getAnnotation(Remotable.class).value()))
                .collect(toList());
    }

    protected Stream<MethodDefinition> scan(Object owner, Class<?> iface, String namespace) {
        return Arrays.stream(iface.getMethods())
                .map(method -> new MethodDefinition(namespace, owner, method, iface));
    }
}
