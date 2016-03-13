package com.github.therapi.core.internal;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypesHelper {
    private static final Pattern QUALIFIED_CLASS_NAME = Pattern.compile("[^<>,\\s]+");

    /**
     * Extracts from the given string anything that looks like a fully-qualified class name.
     */
    public static List<String> getClassNames(String typeName) {
        List<String> classNames = new ArrayList<>();

        Matcher m = QUALIFIED_CLASS_NAME.matcher(typeName);
        while (m.find()) {
            if (m.group().contains(".")) {
                classNames.add(m.group());
            }
        }

        return classNames;
    }

    public static String getSimpleName(String className) {
        if (className.contains("$")) {
            return substringAfterLast(className, "$");
        }

        if (className.contains(".")) {
            return substringAfterLast(className, ".");
        }

        return className;
    }


    /**
     * Returns a Supplier of the default value of {@code type} as defined by JLS --- {@code 0} for numbers, {@code
     * false} for {@code boolean} and {@code '\0'} for {@code char}. For non-primitive types and
     * {@code void}, the supplier returns null.
     */
    public static Supplier<?> getDefaultValueSupplier(Class<?> type) {
        if (type == boolean.class) {
            return () -> false;
        }
        if (type == int.class) {
            return () -> 0;
        }
        if (type == long.class) {
            return () -> 0L;
        }
        if (type == char.class) {
            return () -> '\0';
        }
        if (type == short.class) {
            return () -> (short) 0;
        }
        if (type == byte.class) {
            return () -> (byte) 0;
        }
        if (type == double.class) {
            return () -> 0d;
        }
        if (type == float.class) {
            return () -> 0f;
        }
        return () -> null;
    }

    public static String toJsonType(Type type) {
        String typeName = type.toString();

        typeName = removeStart(typeName, "class ");
        typeName = removeStart(typeName, "interface ");

        if (typeName.equals("int") || typeName.equals("long")) {
            return "integer";
        }

        if (typeName.equals("float") || typeName.equals("double")) {
            return "number";
        }

        typeName = typeName.replace("java.lang.Object", "any");

        typeName = typeName.replace("java.lang.String", "string");

        typeName = typeName.replace("java.lang.Integer", "integer");
        typeName = typeName.replace("java.lang.Long", "integer");

        typeName = typeName.replace("java.lang.Float", "number");
        typeName = typeName.replace("java.lang.Double", "number");

        typeName = typeName.replace("java.util.Set", "array");
        typeName = typeName.replace("java.util.List", "array");
        typeName = typeName.replace("java.util.Collection", "array");

        typeName = typeName.replace("java.util.Map", "map");

        typeName = typeName.replace("java.util.Optional", "optional");
        typeName = typeName.replace("com.google.common.base.Optional", "optional");

        if (typeName.startsWith("com.google.common.collect.Multimap")) {
            String params = substringBetween(typeName, "<", ">");
            String keyType = substringBefore(params, ",").trim();
            String valueType = substringAfter(params, ",").trim();
            typeName = "map<" + keyType + ", array<" + valueType + ">>";
        }

        return typeName;
    }
}
