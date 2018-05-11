package Aggregation.DataStore;

import Aggregation.dataType.DataType;
import Database.MusicBrainzDB;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class DataStore<E extends DataType> implements Iterable<E> {
    private final int lowerID;
    private final int higherID;

    @Getter
    final Map<String, E> map = new HashMap<>();

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

    @Override
    public Iterator<E> iterator() {
        return map.values().iterator();
    }
}
