package com.github.therapi.core.internal;

import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class LangHelper {
    /**
     * Returns the first element in {@code iterable}, or {@code null} if {@code iterable} is empty or {@code null}.
     */
    public static <T> T first(@Nullable Iterable<T> iterable) {
        return iterable == null ? null : Iterables.getFirst(iterable, null);
    }

    public static <K, V> Map<K, V> index(Collection<V> collection, Function<V, K> keyGenerator) {
        return collection.stream().collect(toMap(keyGenerator, identity()));
    }
}
