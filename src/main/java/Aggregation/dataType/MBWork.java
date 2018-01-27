package Aggregation.dataType;

import Search.Term;
import Search.Work;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(of = {"gid"}, callSuper = false)
@ToString
public class MBWork extends DataType {

    private final List<String> artists = new ArrayList<>();
    private final List<String> composers = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    @Getter
    @JsonIgnore
    private String gid;

    public MBWork(String gid) {
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

    @JsonIgnore
    public Collection<String> getComposerTokens() {
        return getTokensFromList(composers);
    }

    @JsonIgnore
    public Collection<String> getNameTokens() {
        return getTokensFromList(names);
    }

    @JsonIgnore
    public Collection<String> getArtistTokens() {
        return getTokensFromList(artists);
    }

    public Work toSearchWork() {
        List<String> terms = new ArrayList<>(getComposerTokens());
        terms.addAll(getNameTokens());
        terms.addAll(getArtistTokens());
        Work work = new Work(gid, terms.size());
        for (String term : terms) {
            work.addTermCount(new Term(1, term), 1);
        }
        return work;
    }

    @Override
    public String jsonSearchRepr() {
        JsonSerializer serializer = JacksonSerializer.getInstance();

        return serializer.writeAsString(this);
    }
}
