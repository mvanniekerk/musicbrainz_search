package Search;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(of = "term")
@ToString
public class Term {
    @Getter
    private final int frequency;
    @Getter
    private final String term;
}
