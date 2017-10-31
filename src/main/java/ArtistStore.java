import dataType.Artist;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class ArtistStore {

    private final Map<String, Artist> artists = new HashMap<>();

    public void aggregateFromDB() throws SQLException {
        Connection conn = MusicBrainzDB.getConnection();

        Statement statement = conn.createStatement();

        statement.setFetchSize(500);

        ResultSet resultSet = statement.executeQuery(
                "SELECT artist_alias.name, artist.gid " +
                "FROM artist " +
                "LEFT JOIN artist_alias ON artist.id = artist_alias.artist"
        );

        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            Artist artist = find(gid);
            String name = resultSet.getString("name");
            artist.addName(name);
        }
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
        ArtistStore artistStore = new ArtistStore();

        artistStore.aggregateFromDB();
    }
}
