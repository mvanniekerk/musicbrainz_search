package Aggregation.DataStore;

import Aggregation.dataType.Artist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArtistStore extends DataStore implements Iterable<Artist> {

    private final Map<String, Artist> artists;

    public ArtistStore(int lowerID, int higherID) {
       super(lowerID, higherID);
       artists = new HashMap<>();
    }

    private PreparedStatement getArtistAliases() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
            "SELECT artist_alias.name, artist.gid " +
                "FROM artist " +
                "LEFT JOIN artist_alias ON artist.id = artist_alias.artist " +
                "WHERE (artist.id >= ?) AND (artist.id < ?)"
        );
    }

    private PreparedStatement getArtistCredits() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
            "select artist_credit_name.name, artist.gid " +
                "from artist " +
                "left join artist_credit_name on artist_credit_name.artist = artist.id " +
                "WHERE (artist.id >= ?) AND (artist.id < ?)"
        );
    }

    private void populateHashMap(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            assert gid != null;
            Artist artist = find(gid);
            String name = resultSet.getString("name");
            if (name != null) {
                artist.addName(name);
            }
        }
    }

    void aggregateFromDB(int from, int to) throws SQLException {
        ResultSet artist_aliases = executePreparedStatement(getArtistAliases(), from, to);
        populateHashMap(artist_aliases);

        ResultSet artist_credits = executePreparedStatement(getArtistCredits(), from, to);
        populateHashMap(artist_credits);
    }

    private Artist find(String gid) {
        Artist result = artists.get(gid);

        if (result == null) {
            result = new Artist(gid);
            artists.put(gid, result);
        }

        return result;
    }

    @Override
    public Iterator<Artist> iterator() {
        return artists.values().iterator();
    }

    public static void main(String[] args) throws Exception {
        ArtistStore artistStore = new ArtistStore(0, 150);

        artistStore.aggregateFromDB();

        for (Artist artist : artistStore) {
            String json = artist.jsonSearchRepr();

            System.out.println(json);
        }
    }
}
