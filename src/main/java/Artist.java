import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@EqualsAndHashCode(of = {"GID"})
public class Artist implements DataType{
    private final Set<String> names = new HashSet<>();

    @Getter
    @Setter
    private String GID;

    public void addName(String name) {
        names.add(name);
    }

    @Override
    public byte[] jsonSearchRepr() {
        return new byte[0];
    }
}
