package Database;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

public class MultiTermSearcher extends Searcher {

    @Override
    QueryBuilder buildSearchQuery(String query, String composerQuery, String artistQuery) {
        return QueryBuilders.multiMatchQuery(query)
                .field("artists.folded", (float) 1.5)
                .field("composers.folded", 1)
                .field("names.folded", 2)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS);
    }
}
