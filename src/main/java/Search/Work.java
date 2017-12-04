package Search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@EqualsAndHashCode(of = {"gid"} )
@ToString
public class Work {
    @Getter
    private String gid;
    @Getter
    private int length;

    // term, termFrequency
    @Getter
    private final Map<Term, Integer> terms = new HashMap<>();

    void addTermCount(Term term, Integer count) {
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
}
