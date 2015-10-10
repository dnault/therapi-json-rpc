package com.github.therapi.example.devilsdictionary;

import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.annotation.Remotable;

import java.util.List;

@Remotable("dictionary")
@SuppressWarnings("unused")
public interface DictionaryService {
    /**
     * Look up the definition of a word in the dictionary.
     *
     * @param word the word to define
     * @return The definition.
     */
    String lookUp(String word);

    /**
     * Search for dictionary entries containing a specific substring.
     * Matching is case insensitive.
     *
     * @param substring the text to search for
     * @return All entries matching the query.
     */
    List<DictionaryEntry> search(String substring);

    /**
     * List the dictionary words, optionally limiting the results to words beginning with the given prefix.
     * The matching is case insensitive.
     *
     * @param prefix the prefix to match
     * @return A list of words that start with the requested prefix, or an empty list if none were found.
     */
    List<String> list(@Default("") String prefix);
}
