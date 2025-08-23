package com.example.f_f.chat.keyword;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class KeywordExtractor {
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "은","는","이","가","을","를","에","에서","와","과","도","으로","하다","있다","입니다",
            "그리고","하지만","또는","혹은","the","a","an","and","or","is","are","to","of","in"
    ));

    public List<String> extract(String text) {
        if (text == null) return List.of();
        String norm = NON_WORD.matcher(text.toLowerCase()).replaceAll(" ").trim();
        Map<String, Integer> freq = new HashMap<>();
        for (String tok : norm.split("\\s+")) {
            if (tok.isBlank() || STOPWORDS.contains(tok)) continue;
            freq.merge(tok, 1, Integer::sum);
        }
        return freq.entrySet().stream()
                .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}