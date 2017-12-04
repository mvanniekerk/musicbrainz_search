package Search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;


@EqualsAndHashCode(of = {"gid"} )
@ToString
public class Work implements Comparable<Work> {
    private static final int TOTAL_N_DOCS = 851366;
    @Getter
    private final String gid;
    @Getter
    private final int length;

    @Getter
    private double tfIdf = 0;

    // term, termFrequency
    @Getter
    private final Map<Term, Integer> terms = new HashMap<>();

    void addTermCount(Term term, Integer count) {
        assert count != 0;
        if (terms.containsKey(term)) {
            int oldCount = terms.get(term);
            terms.replace(term, oldCount + count);
        } else {
            terms.put(term, count);
        }
    }

    Work(String gid, int length) {
        this.gid = gid;
        this.length = length;
    }

    void calculateTfIdf() {
        double result = 0;
        assert length > 0 : "Length should be a natural number";
        for (Map.Entry<Term, Integer> termCount : terms.entrySet()) {
            double tf = (double) termCount.getValue() / length;
            assert termCount.getValue() > 0 : "Term count should be a natural number";
            int count = termCount.getKey().getFrequency();
            assert count > 0 : "Term count (in work) should be a natural number";
            double idf = Math.log10((double) TOTAL_N_DOCS / (double) count);
            double tf_idf = tf * idf;
            result += tf_idf;
        }
        tfIdf = result;
    }

    @Override
    public int compareTo(Work other) {
        int numTerms = Integer.compare(other.terms.size(), this.terms.size());
        if (numTerms != 0) {
            return numTerms;
        }
        return Double.compare(other.tfIdf, this.tfIdf);
    }
}
