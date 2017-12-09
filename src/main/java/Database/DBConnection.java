package Database;

import lombok.Getter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

class DBConnection {
    @Getter
    private Connection connection;

    DBConnection(String url) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver could not be loaded.");
        }

        Properties properties = new Properties();
        properties.setProperty("user", "musicbrainz");
        properties.setProperty("password", "musicbrainz");

        try {
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
