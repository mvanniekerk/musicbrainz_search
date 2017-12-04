package Aggregation.dataType;

import Tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class DataType {
    abstract byte[] jsonSearchRepr();

    public abstract String getGid();

    Collection<String> getTokensFromList(Collection<String> collection) {
        List<String> result = new ArrayList<>();
        for (String composer : collection) {
            String[] tokens = Tokenizer.tokenize(composer);
            result.addAll(Arrays.asList(tokens));
        }

        return result;
    }

}
