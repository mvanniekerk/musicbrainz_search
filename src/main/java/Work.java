import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@EqualsAndHashCode(of = {"GID"})
public class Work implements DataType {

    private final Set<String> artists = new HashSet<>();
    private final Set<String> composers = new HashSet<>();
    private final Set<String> names = new HashSet<>();

    @Getter
    @Setter
    private String GID;

    public void addComposer(String composer) {
        composers.add(composer);
    }

    public void addName(String name) {
        names.add(name);
    }

    public void addArtist(String artist) {
        artists.add(artist);
    }

    @Override
    public byte[] jsonSearchRepr() {
        return new byte[0];
    }
}
