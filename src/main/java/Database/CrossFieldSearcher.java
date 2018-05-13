package Database;

import Tokenizer.Tokenizer;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;

public class CrossFieldSearcher extends Searcher {
    QueryBuilder buildSearchQuery(String query, String composerQuery, String artistQuery) {
        String queryString = String.join(" AND ", Tokenizer.tokenize(query));

        return QueryBuilders.boolQuery().must(
                QueryBuilders.multiMatchQuery(query)
                        .field("artists.folded")
                        .field("composers.folded")
                        .field("names.folded", 1)
                        .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                        .operator(Operator.OR)
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
}
