package com.github.dnault.bozbar;

import static org.springframework.util.ClassUtils.getAllInterfaces;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.dnault.bozbar.annotation.Remotable;
import com.github.dnault.bozbar.internal.BozbarMethod;
import org.springframework.stereotype.Repository;

@Repository
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
        List<BozbarMethod> methods = new ArrayList<>();

        for (Method method : iface.getMethods()) {
            methods.add(new BozbarMethod(namespace, owner, method, iface));
        }

        return methods;
    }
}
