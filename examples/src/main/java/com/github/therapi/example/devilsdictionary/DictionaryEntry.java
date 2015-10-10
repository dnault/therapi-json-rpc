package com.github.therapi.example.devilsdictionary;

import java.util.Map;

public class DictionaryEntry {
    private final String word;
    private final String definition;

    public DictionaryEntry(String word, String definition) {
        this.word = word;
        this.definition = definition;
    }

    public DictionaryEntry(Map.Entry<String,String> e){
        this(e.getKey(), e.getValue());
    }


    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "DictionaryEntry{" +
                "word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}
