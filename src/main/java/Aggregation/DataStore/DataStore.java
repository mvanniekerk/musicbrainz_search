package Aggregation.DataStore;

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
        return MusicBrainzDB.getInstance();
    }

    ResultSet executePreparedStatement(PreparedStatement pstmt, int from, int to) throws SQLException {
        pstmt.setInt(1, from);
        pstmt.setInt(2, to);

        return pstmt.executeQuery();
    }



    public void aggregateFromDB() throws SQLException {
        aggregateFromDB(lowerID, higherID);
    }

    public void aggregateFromDB(int from) throws SQLException {
        aggregateFromDB(from, from+1);
    }

    abstract void aggregateFromDB(int from, int to) throws SQLException;

}
