package Search;

import Database.MusicBrainzDB;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class SearchResult {
    private final Set<String> work_name = new HashSet<>();
    private final Set<String> work_artist = new HashSet<>();
    private final Set<String> work_composer = new HashSet<>();

    @Getter
    private final String term;

    SearchResult(String term) {
        this.term = term;
    }

    public void add(String gid, ResultType type) {
        switch (type) {
            case WORK_ARTIST:
                work_artist.add(gid); break;
            case WORK_NAME:
                work_name.add(gid); break;
            case WORK_COMPOSER:
                work_composer.add(gid); break;
        }
    }

    Connection getConnection() {
        return MusicBrainzDB.getConnection();
    }

    private PreparedStatement getQuery() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
            "INSERT INTO search(search_string, gid, type) " +
                "VALUES (?, ?::uuid, ?) " +
                "ON CONFLICT (search_string, gid) DO NOTHING "
        );
    }

    private void executeQuery(Set<String> set, ResultType resultType) throws SQLException {
        for (String gid : set) {
            PreparedStatement ps = getQuery();
            ps.setString(1, getTerm());
            ps.setObject(2, gid);
            ps.setInt(3, resultType.getIndex());
            ps.executeUpdate();
        }
    }

    void store() throws SQLException {
        executeQuery(work_artist, ResultType.WORK_ARTIST);
        executeQuery(work_composer, ResultType.WORK_COMPOSER);
        executeQuery(work_name, ResultType.WORK_NAME);
    }

    int getResultSize() {
        return work_artist.size() + work_composer.size() + work_name.size();
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "term=" + term +
                ", work_artist=" + work_artist +
                ", work_composer=" + work_composer +
                ", work_name='" + work_name + '\'' +
                '}';
    }
}
