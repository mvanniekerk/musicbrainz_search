package dataType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(of = {"gid"}, callSuper = false)
public class Work extends DataType {

    private final List<String> artists = new ArrayList<>();
    private final List<String> composers = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    @Getter
    @JsonIgnore
    private String gid;

    @Getter
    @Setter
    @JsonIgnore
    private int tokenLength;

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

    public Collection<String> getComposerTokens() {
        return getTokensFromList(composers);
    }

    public Collection<String> getNameTokens() {
        return getTokensFromList(names);
    }

    public Collection<String> getArtistTokens() {
        return getTokensFromList(artists);
    }

    @Override
    public byte[] jsonSearchRepr() {
        JsonSerializer serializer = JacksonSerializer.getInstance();

        return serializer.writeAsBytes(this);
    }

    @Override
    public String toString() {
        return "Work{" +
                "artists=" + artists +
                ", composers=" + composers +
                ", names=" + names +
                ", gid='" + gid + '\'' +
                ", tokenLength=" + tokenLength +
                '}';
    }
}
