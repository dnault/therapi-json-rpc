package com.github.therapi.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.annotation.DoNotLog;
import com.github.therapi.core.annotation.Remotable;
import com.github.therapi.core.internal.TypesHelper;
import com.github.therapi.jackson.enums.CaseFormatHelper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.therapi.core.internal.TypesHelper.findClass;
import static com.github.therapi.core.internal.TypesHelper.isPublic;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class StandardMethodIntrospector implements MethodIntrospector {
    private static final Logger log = LoggerFactory.getLogger(StandardMethodIntrospector.class);

    private final ParameterIntrospector parameterIntrospector;
    private final ProxyIntrospector proxyIntrospector;

    public StandardMethodIntrospector(ObjectMapper mapper) {
        this(new StandardParameterIntrospector(mapper));
    }

    public StandardMethodIntrospector(ParameterIntrospector parameterIntrospector) {
        this.parameterIntrospector = requireNonNull(parameterIntrospector);
        this.proxyIntrospector = getProxyIntrospector();
    }

    protected ProxyIntrospector getProxyIntrospector() {
        final boolean springAopIsPresent = findClass("org.springframework.aop.framework.AopProxyUtils").isPresent();
        return springAopIsPresent ? new SpringAopProxyIntrospector() : Object::getClass;
    }

    protected String getNamespace(Object o, Class<?> targetClass) {
        final Remotable remotable = targetClass.getAnnotation(Remotable.class);
        return remotable == null || remotable.value().equals(Remotable.DEFAULT_NAME)
                ? getDefaultNamespace(targetClass) : remotable.value();
    }

    protected String getDefaultNamespace(Class<?> serviceClass) {
        String className = serviceClass.getSimpleName();
        className = StringUtils.removeEnd(className, "Impl");
        className = StringUtils.removeEnd(className, "Service");
        className = StringUtils.removeEnd(className, "Controller");
        className = CaseFormatHelper.toLowerCamel(className);
        return className;
    }

    @Override
    public Collection<MethodDefinition> findMethods(Object o) {
        final List<MethodDefinition> methodsFromInterfaces = ClassUtils.getAllInterfaces(o.getClass()).stream()
                .filter(iface -> iface.isAnnotationPresent(Remotable.class))
                .flatMap(iface -> findMethodsOnInterface(o, iface, getNamespace(o, iface)))
                .collect(toList());

        final List<MethodDefinition> result = new ArrayList<>(methodsFromInterfaces);

        // Unwrap proxies so we can see annotations on the target class.
        final Class<?> targetClass = proxyIntrospector.getProxyTargetClass(o);

        // For annotated classes, take the service name from the subbiest of subclasses.
        final Remotable remotableClass = targetClass.getAnnotation(Remotable.class);
        if (remotableClass != null) {
            final String namespace = getNamespace(o, targetClass);

            final List<Class<?>> classHierarchy = new ArrayList<>();
            classHierarchy.add(targetClass);
            classHierarchy.addAll(ClassUtils.getAllSuperclasses(targetClass));
            classHierarchy.remove(classHierarchy.size() - 1); // Don't care about Object.class
            Collections.reverse(classHierarchy);

            boolean objectIsJdkDynamicProxy = Proxy.isProxyClass(o.getClass());
            final Map<String, MethodDefinition> methodNameToDef = new HashMap<>();
            for (Class<?> c : classHierarchy) {
                for (Method method : c.getDeclaredMethods()) {
                    final Remotable methodAnnotation = method.getAnnotation(Remotable.class);
                    if (methodAnnotation == null) {
                        continue;
                    }

                    if (!isPublic(method)) {
                        throw new InvalidAnnotationException("Annotation @" + Remotable.class.getName() +
                                " may only be applied to public methods, not " + method);
                    }

                    if (objectIsJdkDynamicProxy && !TypesHelper.findOnInterface(method).isPresent()) {
                        // If Spring created a JDK dynamic proxy whose interfaces do not include the
                        // remotable method, it's not possible for us to invoke the method via the proxy.
                        // We could bypass the proxy, but that could lead to surprising behavior.
                        // For now let's just complain about it.
                        List<Class<?>> proxiedInterfaces = Arrays.stream(o.getClass().getInterfaces())
                                .filter(i -> !i.getName().startsWith("org.springframework."))
                                .collect(toList());

                        throw new InvalidAnnotationException("Annotation @" + Remotable.class.getName() +
                                " on method " + method + " cannot be honored because invocation would bypass the JDK dynamic proxy (created by Spring?)" +
                                " To fix this problem, create the proxy using CGLib or add the remotable method to one of the interfaces being proxied, namely: "
                                + proxiedInterfaces);
                    }

                    String methodName = methodAnnotation.value().equals(Remotable.DEFAULT_NAME)
                            ? method.getName() : methodAnnotation.value();

                    MethodDefinition prevDef = methodNameToDef.put(methodName, new MethodDefinition(
                            namespace, null, method, o, parameterIntrospector.findParameters(method, o),
                            isRequestLoggable(method), isResponseLoggable(method), getCustomAttributes(method)));

                    if (prevDef != null) {
                        log.warn("Remotable method {} with name {} overrides remotable method from superclass (or overloaded method on same class)." +
                                "Previous method definition will be ignored.", method, methodName);
                    }
                }
            }

            result.addAll(methodNameToDef.values());
        }

        return result;
    }

    protected Stream<MethodDefinition> findMethodsOnInterface(Object owner, Class<?> iface, String namespace) {
        return Arrays.stream(iface.getMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(method -> new MethodDefinition(
                        namespace, null, method, owner, parameterIntrospector.findParameters(method, owner),
                        isRequestLoggable(method), isResponseLoggable(method), getCustomAttributes(method)));
    }

    protected ImmutableMap<String, Object> getCustomAttributes(Method method) {
        return ImmutableMap.of();
    }

    protected boolean isRequestLoggable(Method method) {
        DoNotLog doNotLog = method.getAnnotation(DoNotLog.class);
        return doNotLog == null || doNotLog.value() == DoNotLog.Scope.RESPONSE;
    }

    protected boolean isResponseLoggable(Method method) {
        DoNotLog doNotLog = method.getAnnotation(DoNotLog.class);
        return doNotLog == null || doNotLog.value() == DoNotLog.Scope.REQUEST;
    }
}
