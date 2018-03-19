package com.github.therapi.example.devilsdictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.annotation.Remotable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * Demonstrates how the {@link Remotable} annotation may be applied to a class (instead of an interface).
 * When used in this way, each method you wish to expose must be annotated with {@link Remotable}.
 */
@Remotable("dictionary")
public class DictionaryService {
    private final TreeMap<String, String> definitionsByWord = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public DictionaryService() {
        try (InputStream is = getClass().getResourceAsStream("dictionary.json")) {
            this.definitionsByWord.putAll(
                    new ObjectMapper().readValue(is, new TypeReference<Map<String, String>>() {
                    }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Look up the definition of a word in the dictionary.
     *
     * @param word the word to define
     * @return The definition.
     */
    @Remotable
    public String lookUp(String word) {
        return definitionsByWord.get(word);
    }

    /**
     * Search for dictionary entries containing a specific substring.
     * Matching is case insensitive.
     *
     * @param substring the text to search for
     * @return All entries matching the query.
     */
    @Remotable
    public List<DictionaryEntry> search(String substring) {
        return definitionsByWord.entrySet().stream()
                .filter(e -> containsIgnoreCase(e.getKey(), substring) || containsIgnoreCase(e.getValue(), substring))
                .map(DictionaryEntry::new)
                .collect(Collectors.toList());
    }

    /**
     * List the dictionary words, optionally limiting the results to words beginning with the given prefix.
     * The matching is case insensitive.
     *
     * @param prefix the prefix to match
     * @return A list of words that start with the requested prefix, or an empty list if none were found.
     */
    @Remotable
    public List<String> list(@Default("") String prefix) {
        return definitionsByWord.keySet().stream()
                .filter(e -> startsWithIgnoreCase(e, prefix))
                .collect(Collectors.toList());
    }
}
