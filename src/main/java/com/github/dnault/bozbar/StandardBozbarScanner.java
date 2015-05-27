package com.github.dnault.bozbar;

import static java.util.Arrays.stream;
import static org.springframework.util.ClassUtils.getAllInterfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.dnault.bozbar.annotation.Remotable;
import com.github.dnault.bozbar.internal.BozbarMethod;

public class StandardBozbarScanner implements BozbarScanner {
    @Override
    public Collection<BozbarMethod> scan(Object o) {
        List<BozbarMethod> methods = new ArrayList<>();

        for (Class iface : getAllInterfaces(o)) {
            Remotable annotation = (Remotable) iface.getAnnotation(Remotable.class);
            if (annotation != null) {
                methods.addAll(scan(o, iface, annotation.value()));
            }
        }


        return methods;
    }

    protected Collection<BozbarMethod> scan(Object owner, Class iface, String namespace) {
        return stream(iface.getMethods())
                .map(method -> new BozbarMethod(namespace, owner, method, iface))
                .collect(Collectors.toList());
    }
}
