package com.github.therapi.jackson.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CaseFormatHelper {
    public static String toLowerCamel(String name) {
        return toLowerCamel(name, Locale.ROOT);
    }

    public static String toLowerCamel(String name, Locale locale) {
        StringBuilder result = new StringBuilder();
        for (String word : splitWords(name)) {
            result.append(capitalize(word.toLowerCase(locale)));
        }
        result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
        return result.toString();
    }

    private static String capitalize(String s) {
        char first = s.charAt(0);
        return Character.isTitleCase(first) ? s : Character.toTitleCase(first) + s.substring(1);
    }

    public static List<String> splitWords(String name) {
        name = name.replaceAll("_{2,}", "_");
        name = name.replaceAll("-{2,}", "-");

        List<String> result = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder(name.length());

        for (int i = 0, len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            char next = i == len - 1 ? '\0' : name.charAt(i + 1);
            char prev = i == 0 ? '\0' : name.charAt(i - 1);

            if ((c == '_' || c == '-') && !(Character.isDigit(next) && Character.isDigit(prev))) {
                if (currentWord.length() > 0) {
                    result.add(currentWord.toString());
                    currentWord.setLength(0);
                }
                continue;
            }

            if (Character.isUpperCase(c) && (!Character.isUpperCase(prev) || Character.isLowerCase(next))) {
                if (currentWord.length() > 0) {
                    result.add(currentWord.toString());
                    currentWord.setLength(0);
                }
            }

            currentWord.append(c);
        }
        result.add(currentWord.toString());
        return result;
    }
}
