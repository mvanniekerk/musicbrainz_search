package Aggregation.dataType;

import Aggregation.DataStore.WorkStore;
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

import java.sql.Connection;
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

    @Getter
    @JsonIgnore
    private String gid;

    @JsonIgnore
    private Connection conn;

    @JsonIgnore
    private WorkStore workStore;

    @Setter
    @Getter
    @JsonIgnore
    private boolean ignore = false;

    public MBWork(String gid, Connection conn, WorkStore workStore) {
        this.gid = gid;
        this.conn = conn;
        this.workStore = workStore;
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

    public List<Integer> getPartsAsID() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
        "SELECT l_work_work.entity1 FROM work " +
            "JOIN l_work_work ON work.id=l_work_work.entity0 " +
            "JOIN link ON l_work_work.link=link.id " +
            "WHERE work.gid=?::uuid " +
            "AND link.link_type=281"
        );
        ps.setString(1,gid);
        ResultSet rs = ps.executeQuery();

        List<Integer> parts = new ArrayList<>();

        while (rs.next()) {
            parts.add(rs.getInt(1));
        }

        return parts;
    }

    @Override
    public String jsonSearchRepr() {
        JsonSerializer serializer = JacksonSerializer.getInstance();

        return serializer.writeAsString(this);
    }
}
