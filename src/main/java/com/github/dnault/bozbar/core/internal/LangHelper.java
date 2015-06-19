package com.github.dnault.bozbar.core.internal;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

public class LangHelper {
    /**
     * Returns the first element in {@code iterable}, or {@code null} if {@code iterable} is empty or {@code null}.
     */
    public static <T> T first(@Nullable Iterable<T> iterable) {
        return iterable == null ? null : Iterables.getFirst(iterable, null);
    }
}
