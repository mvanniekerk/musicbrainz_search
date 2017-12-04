package Search;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(of = "term")
@ToString
public class TermCount {
    @Getter
    private final int frequency;
    @Getter
    private final Term term;
}
