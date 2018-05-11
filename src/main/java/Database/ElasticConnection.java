package Database;

import Aggregation.DataStore.DataStore;
import Aggregation.dataType.DataType;
import Tokenizer.Tokenizer;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;


public class ElasticConnection {
    @Getter
    private RestHighLevelClient client;
    private final String INDEX = "musicbrainz";
    private final String TYPE = "work";


    private static ElasticConnection elasticConnection;

    private ElasticConnection() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.99.100", 9200, "http")
                )
        );
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ElasticConnection getInstance() {
        if (elasticConnection == null) {
            elasticConnection = new ElasticConnection();
        }
        return elasticConnection;
    }

    public void storeDocument(String json, String id) {
        storeDocument(json, id, INDEX, TYPE);
    }

    public void storeBulk(String index, String type, DataStore<? extends DataType> dataStore) {
        BulkRequest request = new BulkRequest();
        for (DataType dataType : dataStore) {
            request.add(new IndexRequest(index, type, dataType.getGid())
                    .source(dataType.jsonSearchRepr(), XContentType.JSON));
        }

        try {
            client.bulk(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeDocument(String json, String id, String index, String type) {
        IndexRequest request = new IndexRequest(index, type, id);

        request.source(json, XContentType.JSON);

        try {
            client.index(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getDocument(String gid) throws IOException {
        return getDocument(gid, INDEX, TYPE);
    }

    public String getDocument(String gid, String index, String type) throws IOException {
        GetRequest getRequest = new GetRequest(index, type, gid);

        return client.get(getRequest).toString();
    }

    public String recordingSearch(String query, String gid) {
        SearchRequest request = new SearchRequest("mb").types("recording");

        SearchSourceBuilder sb = new SearchSourceBuilder();

        QueryBuilder termQuery = QueryBuilders.termQuery("work_gid.keyword", gid);
        request.source(sb.query(termQuery));
        try {
            return client.search(request).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

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

        return client.search(request).toString();
    }
}
