package DataStore;

import Database.MusicBrainzDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DataStore {
    private final int lowerID;
    private final int higherID;

    DataStore(int lowerID, int higherID) {
        this.lowerID = lowerID;
        this.higherID = higherID;
    }

    Connection getConnection() {
        return MusicBrainzDB.getConnection();
    }

    ResultSet executePreparedStatement(PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, lowerID);
        pstmt.setInt(2, higherID);

        return pstmt.executeQuery();
    }

    abstract void aggregateFromDB() throws SQLException;

}
