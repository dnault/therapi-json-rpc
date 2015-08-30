package com.github.dnault.therapi.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dnault.therapi.core.annotation.Remotable;
import com.github.dnault.therapi.core.internal.MethodDefinition;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class StandardMethodIntrospector implements MethodIntrospector {

    private final ParameterIntrospector parameterIntrospector;

    public StandardMethodIntrospector(ObjectMapper mapper) {
        this(new StandardParameterIntrospector(mapper));
    }

    public StandardMethodIntrospector(ParameterIntrospector parameterIntrospector) {
        this.parameterIntrospector = requireNonNull(parameterIntrospector);
    }

    @Override
    public Collection<MethodDefinition> findMethods(Object o) {
        return ClassUtils.getAllInterfaces(o.getClass()).stream()
                .filter(iface -> iface.isAnnotationPresent(Remotable.class))
                .flatMap(iface -> findMethodsOnInterface(o, iface, iface.getAnnotation(Remotable.class).value()))
                .collect(toList());
    }

    protected Stream<MethodDefinition> findMethodsOnInterface(Object owner, Class<?> iface, String namespace) {
        return Arrays.stream(iface.getMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(method -> new MethodDefinition(namespace, null, method, owner, parameterIntrospector.findParameters(method, owner)));
    }
}
