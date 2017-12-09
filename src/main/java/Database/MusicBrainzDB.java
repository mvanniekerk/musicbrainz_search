package Database;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class MusicBrainzDB extends DBConnection {
    private static final String URL = "jdbc:postgresql:musicbrainz";

    @Nullable
    private static MusicBrainzDB musicBrainzDB;

    private MusicBrainzDB() {
        super(URL);
    }

    public static Connection getInstance() {
        if (musicBrainzDB == null) {
            musicBrainzDB = new MusicBrainzDB();
        }
        return musicBrainzDB.getConnection();
    }

    /**
     * Alive check.
     * @param args no args.
     */
    public static void main(String[] args) throws SQLException {
        Connection conn = MusicBrainzDB.getInstance();
        ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM work WHERE work.id = 357993");

        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }
}
