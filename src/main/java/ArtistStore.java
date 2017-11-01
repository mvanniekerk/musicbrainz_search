import dataType.Artist;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ArtistStore {

    private final Map<String, Artist> artists;
    private final int lowerID;
    private final int higherID;

    ArtistStore(int lowerID, int higherID) {
       artists = new HashMap<>();
       this.lowerID = lowerID;
       this.higherID = higherID;
    }

    Connection getConnection() {
        return MusicBrainzDB.getConnection();
    }

    PreparedStatement getArtistAliases() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
            "SELECT artist_alias.name, artist.gid " +
                "FROM artist " +
                "LEFT JOIN artist_alias ON artist.id = artist_alias.artist " +
                "WHERE (artist.id >= ?) AND (artist.id < ?)"
        );
    }

    PreparedStatement getArtistCredits() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
            "select artist_credit_name.name, artist.gid " +
                "from artist " +
                "left join artist_credit_name on artist_credit_name.artist = artist.id " +
                "WHERE (artist.id >= ?) AND (artist.id < ?)"
        );
    }

    ResultSet executePreparedStatement(PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, lowerID);
        pstmt.setInt(2, higherID);

        return pstmt.executeQuery();
    }

    void populateHashMap(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            Artist artist = find(gid);
            String name = resultSet.getString("name");
            artist.addName(name);
        }
    }

    void aggregateFromDB() throws SQLException {
        ResultSet artist_aliases = executePreparedStatement(getArtistAliases());
        populateHashMap(artist_aliases);

        ResultSet artist_credits = executePreparedStatement(getArtistCredits());
        populateHashMap(artist_credits);
    }

    Artist find(String gid) {
        Artist result = artists.get(gid);

        if (result == null) {
            result = new Artist(gid);
            artists.put(gid, result);
        }

        return result;
    }

    public static void main(String[] args) throws SQLException {
        ArtistStore artistStore = new ArtistStore(0, 500);

        artistStore.aggregateFromDB();
    }
}
