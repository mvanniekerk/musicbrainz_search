package Tokenizer;

import lombok.AllArgsConstructor;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class Tokenizer {
    private static final Map<Character, Character> strangeCharMap;
    static {
        final Map<Character, Character> map = new HashMap<>();
        map.put('\u00F8', 'o');
        map.put('\u00D8', 'O');
        strangeCharMap = Collections.unmodifiableMap(map);
    }

    public static String[] tokenize(String string) {
        if (string.isEmpty()) {
            return new String[0];
        }

        String lemmatized = lemmatize(string);
        String[] result = lemmatized.split("[^a-z|'\\.|0-9]+");
        return stripBadResult(result);
    }

    private static String[] stripBadResult(String[] result) {
        if (result.length == 0) {
            return result;
        } else if (result[0].equals("")) {
            return Arrays.copyOfRange(result, 1, result.length);
        } else {
            return result;
        }
    }

    static String lemmatize(String string) {
        return toAscii(string).toLowerCase();
    }

    static String toAscii(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);
        for (char c : string.toCharArray()) {
            // remove all unicode characters and modifiers.
            if (c <= '\u007F') {
                sb.append(c);
            // transform some unicode characters to ascii
            } else if (strangeCharMap.containsKey(c)) {
                sb.append(strangeCharMap.get(c));
            } else if (Character.isLetter(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
