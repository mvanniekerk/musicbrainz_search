package Database;

import Tokenizer.Tokenizer;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public class LuceneSearcher implements Searcher {
    private final String INDEX = "musicbrainz";
    private final String TYPE = "work";

    private QueryBuilder buildSearchQuery(String queryString, String composerQuery, String artistQuery) {
        return QueryBuilders.boolQuery().must(
                QueryBuilders.queryStringQuery(queryString)
                        .field("artists.folded")
                        .field("composers.folded")
                        .field("names.folded")
        ).must(
                QueryBuilders.matchQuery("composers.folded", composerQuery)
                        .operator(Operator.OR)
                        .zeroTermsQuery(MatchQuery.ZeroTermsQuery.ALL)
        ).must(
                QueryBuilders.matchQuery("artists.folded", artistQuery)
                        .operator(Operator.OR)
                        .zeroTermsQuery(MatchQuery.ZeroTermsQuery.ALL)
        );
    }

    public String search(
            String query, String composerQuery, String artistQuery, int from, int size) throws IOException {

        String queryString = String.join(" AND ", Tokenizer.tokenize(query));

        SearchRequest request = new SearchRequest(INDEX);
        request.types(TYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        QueryBuilder boolQuery = buildSearchQuery(queryString, artistQuery, composerQuery);

        searchSourceBuilder.query(boolQuery);

        request.source(searchSourceBuilder);

        return ElasticConnection.getInstance().getClient().search(request).toString();
    }
}
