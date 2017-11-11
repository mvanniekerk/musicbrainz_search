import dataType.DataType;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.AllArgsConstructor;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class Tokenizer {
    private DataType dataType;

    private static final Map<Character, Character> strangeCharMap;
    static {
        final Map<Character, Character> map = new HashMap<>();
        map.put('\u00F8', 'o');
        map.put('\u00D8', 'O');
        strangeCharMap = Collections.unmodifiableMap(map);
    }

    static String toAscii(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
            else if (strangeCharMap.containsKey(c)) {
                sb.append(strangeCharMap.get(c));
            }
            else if (Character.isLetter(c)) sb.append(c);
        }
        return sb.toString();
    }
}
