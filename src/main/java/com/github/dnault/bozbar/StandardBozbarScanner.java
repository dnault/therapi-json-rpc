package com.github.dnault.bozbar;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.ClassUtils.getAllInterfaces;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import com.github.dnault.bozbar.annotation.Remotable;
import com.github.dnault.bozbar.internal.BozbarMethod;

public class StandardBozbarScanner implements BozbarScanner {
    @Override
    public Collection<BozbarMethod> scan(Object o) {
        return Arrays.stream(getAllInterfaces(o))
                .filter(iface -> iface.isAnnotationPresent(Remotable.class))
                .flatMap(iface -> scan(o, iface, iface.getAnnotation(Remotable.class).value()))
                .collect(toList());
    }

    protected Stream<BozbarMethod> scan(Object owner, Class<?> iface, String namespace) {
        return Arrays.stream(iface.getMethods())
                .map(method -> new BozbarMethod(namespace, owner, method, iface));
    }
}
