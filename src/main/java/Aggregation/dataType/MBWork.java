package Aggregation.dataType;

import Database.MusicBrainzDB;
import Search.Term;
import Search.Work;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(of = {"gid"}, callSuper = false)
@ToString
public class MBWork extends DataType {

    private final List<String> artists = new ArrayList<>();
    private final List<String> composers = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    @Nullable
    @Setter
    private String name;
    @Nullable
    @Setter
    private String artist;

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

    public void retrieveWorkArtist() throws SQLException {
        PreparedStatement ps = MusicBrainzDB.getInstance().prepareStatement(
                "SELECT link_type.name, artist.name\n" +
                        "FROM work\n" +
                        "  JOIN l_artist_work ON entity1=work.id\n" +
                        "  JOIN artist ON entity0=artist.id\n" +
                        "  JOIN link ON l_artist_work.link=link.id\n" +
                        "  JOIN link_type ON link.link_type=link_type.id\n" +
                        "WHERE work.gid = ?::uuid"
        );
        ps.setString(1, gid);
        ResultSet rs = ps.executeQuery();
        StringBuilder result = new StringBuilder();

        while (rs.next()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(rs.getString(1))
                    .append(": ")
                    .append(rs.getString(2));
        }
        artist = result.toString();
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
