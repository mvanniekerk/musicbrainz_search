package dataType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.emory.mathcs.backport.java.util.Arrays;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = {"gid"}, callSuper = false)
public class Work extends DataType {

    private final Set<String> artists = new HashSet<>();
    private final Set<String> composers = new HashSet<>();
    private final Set<String> names = new HashSet<>();

    @Getter
    @JsonIgnore
    private String gid;

    public Work(String gid) {
        this.gid = gid;
    }

    public void addComposer(String composer) {
        composers.add(composer);
    }

    public void addName(String name) {
        names.add(name);
    }

    public void addArtist(String artist) {
        artists.add(artist);
    }

    public Set<String> getComposerTokens() {
        return getTokensFromSet(composers);
    }

    public Set<String> getNameTokens() {
        return getTokensFromSet(names);
    }

    public Set<String> getArtistTokens() {
        return getTokensFromSet(artists);
    }

    @Override
    public byte[] jsonSearchRepr() {
        JsonSerializer serializer = JacksonSerializer.getInstance();

        return serializer.writeAsBytes(this);
    }
}
