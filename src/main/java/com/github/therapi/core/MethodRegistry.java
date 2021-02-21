package com.github.therapi.core;

import static com.github.therapi.core.internal.JacksonHelper.isLikeNull;
import static com.github.therapi.core.internal.LangHelper.propagate;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.github.therapi.core.annotation.ExampleModel;
import com.github.therapi.core.interceptor.SimpleMethodInvocation;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodRegistry {
    private static final Logger log = LoggerFactory.getLogger(MethodRegistry.class);

    private final HashMap<String, MethodDefinition> methodsByName = new HashMap<>();

    private MethodIntrospector scanner;
    private final ObjectMapper objectMapper;
    private String namespaceSeparator = ".";

    private final ArrayListMultimap<Class, Method> modelClassToExampleFactoryMethods
            = ArrayListMultimap.create();

    public ImmutableList<Method> getExampleFactoryMethods(Class modelClass) {
        return ImmutableList.copyOf(modelClassToExampleFactoryMethods.get(modelClass));
    }

    private static class InterceptorRegistration {
        private final Predicate<MethodDefinition> predicate;
        private final MethodInterceptor interceptor;

        private InterceptorRegistration(Predicate<MethodDefinition> predicate, MethodInterceptor interceptor) {
            this.predicate = predicate;
            this.interceptor = interceptor;
        }
    }

    // populated as the user registers interceptors
    private final List<InterceptorRegistration> interceptorRegistrations = new ArrayList<>();

    // populated on-the-fly based on the values in interceptorRegistrations at the time a method is first invoked
    private final ConcurrentMap<MethodDefinition, ImmutableList<MethodInterceptor>> methodDefinitionToInterceptors = new ConcurrentHashMap<>();

    private boolean suggestMethods = true;

    public boolean isSuggestMethods() {
        return suggestMethods;
    }

    public void setSuggestMethods(boolean suggestMethods) {
        this.suggestMethods = suggestMethods;
    }

    public MethodRegistry() {
        this(new ObjectMapper());
    }

    public MethodRegistry(ObjectMapper objectMapper) {
        this(objectMapper, new StandardMethodIntrospector(objectMapper));
    }

    public MethodRegistry(ObjectMapper objectMapper, MethodIntrospector methodIntrospector) {
        this.objectMapper = requireNonNull(objectMapper);
        this.scanner = methodIntrospector;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Registers the given method interceptor to be applied to all methods
     * matching the given predicate. Interceptors will be invoked in the same order
     * they are registered.
     * <p>
     * The effective list of interceptors for a method is determined when the method
     * is first invoked; subsequent interceptor registrations will not affect the method.
     * <p>
     * Here's an example that matches any method and prints how long the invocation takes:
     * <pre>
     * methodRegistry.intercept(MethodPredicates.any(), invocation -&gt; {
     *   Stopwatch timer = Stopwatch.createStarted();
     *   String methodName = MethodDefinitionInvocation.getQualifiedName(invocation);
     *   try {
     *     return invocation.proceed();
     *   } finally {
     *     System.out.println("Method '" + methodName + "' completed in " + timer);
     *   }
     * });
     * </pre>
     *
     * @see com.github.therapi.core.interceptor.MethodPredicates
     */
    public void intercept(Predicate<MethodDefinition> predicate, MethodInterceptor interceptor) {
        requireNonNull(predicate);
        requireNonNull(interceptor);
        interceptorRegistrations.add(new InterceptorRegistration(predicate, interceptor));
    }

    protected ImmutableList<MethodInterceptor> getInterceptors(MethodDefinition methodDef) {
        return computeIfAbsent(methodDefinitionToInterceptors, methodDef, methodDefintion -> {
            ImmutableList.Builder<MethodInterceptor> builder = ImmutableList.builder();
            interceptorRegistrations.stream()
                    .filter(registration -> registration.predicate.test(methodDefintion))
                    .forEach(registration -> builder.add(registration.interceptor));
            return builder.build();
        });
    }

    protected MethodInvocation newMethodInvocation(MethodDefinition methodDef, Object[] args, List<MethodInterceptor> interceptors) {
        return new SimpleMethodInvocation(methodDef, args, interceptors);
    }

    /**
     * Unlike {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} this method
     * is optimized for the case where the entry already exists, and does not always
     * use synchronization.
     */
    private static <K, V> V computeIfAbsent(ConcurrentMap<K, V> map, K key, Function<? super K, ? extends V> mappingFunction) {
        V result = map.get(key);
        return result != null ? result : map.computeIfAbsent(key, mappingFunction);
    }

    public List<String> scan(Object o) {
        scanForExampleModels(o);

        List<String> methodNames = new ArrayList<>();
        for (MethodDefinition methodDef : scanner.findMethods(o)) {
            add(methodDef);
            methodNames.add(getName(methodDef));
        }
        return methodNames;
    }

    protected void scanForExampleModels(Object o) {
        List<Class<?>> classesToScan = ClassUtils.getAllInterfaces(o.getClass());
        classesToScan.add(o.getClass());
        for (Class<?> scanMe : classesToScan) {
            for (Method m : scanMe.getMethods()) {
                ExampleModel exampleModel = m.getAnnotation(ExampleModel.class);
                if (exampleModel != null) {
                    if (!isStatic(m.getModifiers()) || !isPublic(m.getModifiers())) {
                        throw new IllegalArgumentException(
                                "@ExampleModel annotation may only be applied to public static method, not " + m);
                    }
                    Class modelClass = m.getReturnType();
                    modelClassToExampleFactoryMethods.put(modelClass, m);
                }
            }
        }
    }

    private void add(MethodDefinition methodDef) {
        methodsByName.put(getName(methodDef), methodDef);
    }

    public List<String> suggestMethods(String methodName) {
        TreeMultimap<Integer, String> suggestionsByDistance = TreeMultimap.create();
        for (String name : methodsByName.keySet()) {
            int distance = new LevenshteinDistance(25).apply(name, methodName);
            if (distance != -1) {
                suggestionsByDistance.put(distance, name);
            }
        }

        return suggestionsByDistance.entries().stream()
                .limit(5)
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    public JsonNode invoke(String methodName, JsonNode args) throws MethodNotFoundException {
        if (!args.isArray() && !args.isObject()) {
            throw new IllegalArgumentException("arguments must be ARRAY or OBJECT but encountered " + args.getNodeType());
        }

        MethodDefinition method = methodsByName.get(methodName);
        if (method == null) {
            throw MethodNotFoundException.forMethod(methodName, suggestMethods ? suggestMethods(methodName) : null);
        }

        Object[] boundArgs = bindArgs(method, args);
        return invoke(method, boundArgs);
    }

    private JsonNode invoke(MethodDefinition method, Object[] args) {
        try {
            MethodInvocation invocation = newMethodInvocation(method, args, getInterceptors(method));
            Object result = invocation.proceed();

            TokenBuffer buffer = new TokenBuffer(objectMapper, false);
            objectMapper.writerFor(method.getReturnTypeRef()).writeValue(buffer, result);
            return objectMapper.readTree(buffer.asParser());

        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    private Object[] bindArgs(MethodDefinition method, JsonNode args) {
        if (args.isArray()) {
            return bindPositionalArguments(method, (ArrayNode) args);
        }

        return bindNamedArguments(method, (ObjectNode) args);
    }

    private Object[] bindNamedArguments(MethodDefinition method, ObjectNode args) {
        Object[] boundArgs = new Object[method.getParameters().size()];
        List<ParameterDefinition> params = method.getParameters();

        int consumedArgCount = 0;
        int i = 0;
        for (ParameterDefinition p : params) {
            JsonNode arg = args.get(p.getName());

            if (!args.has(p.getName())) {
                if (p.getDefaultValueSupplier().isPresent()) {
                    boundArgs[i++] = p.getDefaultValueSupplier().get().get();
                    continue;
                } else {
                    throw new MissingArgumentException(p.getName());
                }
            }

            if (isLikeNull(arg) && !p.isNullable()) {
                throw new NullArgumentException(p.getName());
            }

            try {
                boundArgs[i++] = objectMapper.convertValue(arg, p.getType());
                consumedArgCount++;
            } catch (Exception e) {
                throw new ParameterBindingException(p.getName(), buildParamBindingErrorMessage(p, arg, e));
            }
        }

        if (consumedArgCount != args.size()) {
            Set<String> parameterNames = params.stream().map(ParameterDefinition::getName).collect(toSet());
            Set<String> argumentNames = ImmutableSet.copyOf(args.fieldNames());
            Set<String> extraArguments = Sets.difference(argumentNames, parameterNames);
            if (!extraArguments.isEmpty()) {
                throw new ParameterBindingException(null, "unrecognized argument names: " + extraArguments);
            }
        }

        return boundArgs;
    }

    private String getName(MethodDefinition method) {
        return method.getQualifiedName(namespaceSeparator);
    }

    private Object[] bindPositionalArguments(MethodDefinition method, ArrayNode args) {
        Object[] boundArgs = new Object[method.getParameters().size()];
        List<ParameterDefinition> params = method.getParameters();

        if (args.size() > params.size()) {
            throw new TooManyPositionalArguments(params.size(), args.size());
        }

        for (int i = 0; i < params.size(); i++) {
            ParameterDefinition param = params.get(i);

            if (!args.has(i)) {
                if (param.getDefaultValueSupplier().isPresent()) {
                    boundArgs[i] = param.getDefaultValueSupplier().get().get();
                    continue;
                } else {
                    throw new MissingArgumentException(param.getName());
                }
            }

            JsonNode arg = args.get(i);
            if (isLikeNull(arg) && !param.isNullable()) {
                throw new NullArgumentException(param.getName());
            }

            try {
                boundArgs[i] = objectMapper.convertValue(arg, param.getType());
            } catch (Exception e) {
                throw new ParameterBindingException(param.getName(), buildParamBindingErrorMessage(param, arg, e));
            }
        }
        return boundArgs;
    }

    private String buildParamBindingErrorMessage(ParameterDefinition param, JsonNode arg, Exception e) {
        String jacksonErrorMessage = e.getMessage().replace("\n at [Source: N/A; line: -1, column: -1]", "");

        String typeName = param.getType().getType().toString();
        return "Can't bind parameter '" + param.getName() + "' of type " + typeName + " to "
                + arg.getNodeType() + " value " + arg.toString() + " : " + jacksonErrorMessage;
    }

    public Collection<MethodDefinition> getMethods() {
        return unmodifiableCollection(methodsByName.values());
    }

    public Optional<MethodDefinition> getMethod(String methodName) {
        return Optional.ofNullable(methodsByName.get(methodName));
    }
}
