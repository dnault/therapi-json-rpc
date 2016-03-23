package com.github.therapi.core.internal;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class LangHelper {
    public static <K, V> Map<K, V> index(Collection<V> collection, Function<V, K> keyGenerator) {
        return collection.stream().collect(toMap(keyGenerator, identity()));
    }
}
