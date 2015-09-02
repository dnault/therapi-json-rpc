package com.github.dnault.therapi.example.devilsdictionary;

import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.annotation.Remotable;

import java.util.List;

@Remotable("dictionary")
@SuppressWarnings("unused")
public interface DictionaryService {
    String lookUp(String word);

    List<DictionaryEntry> search(String substring);

    List<String> list(@Default("") String prefix);
}
