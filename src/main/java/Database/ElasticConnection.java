package Database;

import Aggregation.DataStore.WorkStore;
import Aggregation.dataType.MBWork;
import Tokenizer.Tokenizer;
import org.apache.http.HttpHost;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


public class ElasticConnection {
    private RestHighLevelClient client;
    private final String INDEX = "musicbrainz";
    private final String TYPE = "work";

    @Nullable
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
        IndexRequest request = new IndexRequest(INDEX, TYPE, id);

        request.source(json, XContentType.JSON);

        try {
            client.index(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String search(String query) throws IOException {

        String queryString = String.join(" AND ", Tokenizer.tokenize(query));

        SearchRequest request = new SearchRequest(INDEX);
        request.types(TYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.queryStringQuery(queryString)
                        .field("artists")
                        .field("composers")
                        .field("names")
        );
        request.source(searchSourceBuilder);

        try {
            return client.search(request).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        String response = ElasticConnection.getInstance().search("haydn");
        System.out.println(response);
        ElasticConnection.getInstance().close();
    }
}
