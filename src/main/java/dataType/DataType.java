package dataType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class DataType {
    abstract byte[] jsonSearchRepr();

    abstract String getGid();

    Set<String> getTokensFromSet(Set<String> collection) {
        Set<String> result = new HashSet<>();
        for (String composer : collection) {
            String[] tokens = Tokenizer.tokenize(composer);
            result.addAll(Arrays.asList(tokens));
        }

        return result;
    }

}
