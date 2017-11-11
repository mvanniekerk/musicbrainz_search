package Database;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class MusicBrainzDB {

    @Nullable
    private static MusicBrainzDB musicBrainzDB;

    private Connection connection;

    private MusicBrainzDB() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver could not be loaded.");
        }

        String url = "jdbc:postgresql:";
        Properties properties = new Properties();
        properties.setProperty("user", "musicbrainz");
        properties.setProperty("password", "musicbrainz");

        try {
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() {
        if (musicBrainzDB == null) {
            musicBrainzDB = new MusicBrainzDB();
        }
        return musicBrainzDB.connection;
    }

    /**
     * Alive check.
     * @param args no args.
     */
    public static void main(String[] args) throws SQLException {
        Connection conn = MusicBrainzDB.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM work WHERE work.id = 357993");

        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }
}
