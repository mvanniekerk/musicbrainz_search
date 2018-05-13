package Database;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;

public class CrossFieldSearcher extends Searcher {

    public CrossFieldSearcher(float artistBoost, float composerBoost, float namesBoost) {
        super(artistBoost, composerBoost, namesBoost);
    }

    QueryBuilder buildSearchQuery(String query, String composerQuery, String artistQuery) {
        return QueryBuilders.boolQuery().must(
                QueryBuilders.multiMatchQuery(query)
                        .field("artists.folded", getArtistBoost())
                        .field("composers.folded", getComposerBoost())
                        .field("names.folded", getNamesBoost())
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
