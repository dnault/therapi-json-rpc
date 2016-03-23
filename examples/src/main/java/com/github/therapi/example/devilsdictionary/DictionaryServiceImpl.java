package com.github.therapi.example.devilsdictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.annotation.Default;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.google.common.base.Throwables.propagate;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

public class DictionaryServiceImpl implements DictionaryService {
    private final TreeMap<String, String> definitionsByWord = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public DictionaryServiceImpl() {
        try (InputStream is = getClass().getResourceAsStream("dictionary.json")) {
            this.definitionsByWord.putAll(
                    new ObjectMapper().readValue(is, new TypeReference<Map<String, String>>() {
                    }));
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    @Override
    public String lookUp(String word) {
        return definitionsByWord.get(word);
    }

    @Override
    public List<DictionaryEntry> search(String substring) {
        return definitionsByWord.entrySet().stream()
                .filter(e -> containsIgnoreCase(e.getKey(), substring) || containsIgnoreCase(e.getValue(), substring))
                .map(DictionaryEntry::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> list(@Default("") String prefix) {
        return definitionsByWord.keySet().stream()
                .filter(e -> startsWithIgnoreCase(e, prefix))
                .collect(Collectors.toList());
    }
}
