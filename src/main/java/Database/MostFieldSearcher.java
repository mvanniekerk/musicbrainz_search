package Database;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;


public class MostFieldSearcher extends Searcher {
    public MostFieldSearcher(float artistBoost, float composerBoost, float namesBoost) {
        super(artistBoost, composerBoost, namesBoost);
    }

    @Override
    QueryBuilder buildSearchQuery(String query, String composerQuery, String artistQuery) {
        return QueryBuilders.multiMatchQuery(query)
                .field("artists.folded", getArtistBoost())
                .field("composers.folded", getComposerBoost())
                .field("names.folded", getNamesBoost())
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS);
    }
}
