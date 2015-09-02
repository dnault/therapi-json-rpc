package com.github.dnault.therapi.example.devilsdictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Parses the full text of the Devil's Dictionary into discrete entries
 */
public class Parser {
    public static void main(String[] args) throws Exception {
        String dictionary = getDictionary();
        //    System.out.println(dictionary);

        Map<String, DictionaryEntry> indexByWord = new LinkedHashMap<>();
        Map<String, String> asMap = new LinkedHashMap<>();
        for (String entry : splitEntries(dictionary)) {
            String word = getWord(entry);
            String definition = StringUtils.removeStart(entry, word);
            definition = StringUtils.removeStart(definition, ",");
            definition = definition.trim();

          //  definition = unwrap(definition);

            DictionaryEntry e = new DictionaryEntry(word, definition);
            indexByWord.put(word, e);
            asMap.put(word, definition);
        }



        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(asMap));
    }

    private static String getWord(String s) {
        Pattern p = Pattern.compile("[^, ]+");
        Matcher m = p.matcher(s);
        boolean found = m.find();
        if (!found) {
            throw new IllegalArgumentException("didn't find word in: " + s);
        }
        return m.group(0);
    }

    private static boolean isStartOfDefinition(String line) {

        if (line.isEmpty() || !Character.isAlphabetic(line.charAt(0))) {
            return false;
        }

        // special cases
        if (line.startsWith("X in our alphabet") || line.startsWith("W (double U)")) {
            return true;
        }
        if (!line.contains(",")) {
            return false;
        }
        String s = StringUtils.substringBefore(line, ",");
        return !s.isEmpty() && !s.contains(" ") && s.equals(s.toUpperCase(Locale.ROOT));
    }


    public static String getDictionary() throws IOException {
        try (InputStream is = Parser.class.getResourceAsStream("dictionary.txt")) {
            String fullText = IOUtils.toString(is, StandardCharsets.UTF_8);


            return StringUtils.substringBetween(fullText, "A.B.", "End of Project Gutenberg's The Devil's Dictionary").trim();
        }
    }

    public static List<String> splitEntries(String dictionary) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(dictionary));

        String line;

        List<String> entries = new ArrayList<>();
        StringBuilder entry = new StringBuilder();
        boolean prevLineWasBlank = false;
        while ((line = r.readLine()) != null) {
            if (line.trim().length() == 1) {
                // letter section headings
                continue;
            }


            if (prevLineWasBlank && isStartOfDefinition(line)) {
                String s = entry.toString().trim();
                if (isNotBlank(s)) {
                    entries.add(s);
                }
                entry = new StringBuilder();
            }

            prevLineWasBlank = line.isEmpty();

            entry.append(line).append("\n");

        }

        String s = entry.toString().trim();
        if (isNotBlank(s)) {
            entries.add(s);
        }
        return entries;
    }
}
