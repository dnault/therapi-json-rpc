package com.github.therapi.core.internal;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangHelper {
    public static <K, V> Map<K, V> index(Collection<V> collection, Function<V, K> keyGenerator) {
        return collection.stream().collect(toMap(keyGenerator, identity()));
    }

    public static String replace(String input, Pattern pattern, Function<Matcher, String> replacementGenerator) {
        StringBuffer sb = new StringBuffer();
        Matcher m = pattern.matcher(input);
        while (m.find()) {
            m.appendReplacement(sb, replacementGenerator.apply(m));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
