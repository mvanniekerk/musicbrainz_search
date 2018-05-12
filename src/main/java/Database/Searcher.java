package Database;

import java.io.IOException;

public interface Searcher {
    String search(
            String query, String composerQuery, String artistQuery, int from, int size) throws IOException;
}
