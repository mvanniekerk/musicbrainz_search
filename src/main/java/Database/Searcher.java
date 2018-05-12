package Database;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public abstract class Searcher {
    private final String INDEX = "musicbrainz";
    private final String TYPE = "work";

    abstract QueryBuilder buildSearchQuery(String query, String composerQuery, String artistQuery);

    public String search(
            String query, String composerQuery, String artistQuery, int from, int size) throws IOException {

        SearchRequest request = new SearchRequest(INDEX);
        request.types(TYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        QueryBuilder boolQuery = buildSearchQuery(query, artistQuery, composerQuery);

        searchSourceBuilder.query(boolQuery);

        request.source(searchSourceBuilder);

        return ElasticConnection.getInstance().getClient().search(request).toString();
    }
}
