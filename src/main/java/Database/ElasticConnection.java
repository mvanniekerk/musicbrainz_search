package Database;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;


public class ElasticConnection {
    private RestHighLevelClient client;
    private final String INDEX = "mb";
    private final String TYPE = "work";



    public ElasticConnection() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.99.100", 9200, "http")
                )
        );
    }

    void storeDocument(String json, String id) throws IOException {
        IndexRequest request = new IndexRequest(INDEX, TYPE, id);

        request.source(json);

        IndexResponse indexResponse = client.index(request);
    }

    public static void main(String[] args) throws Exception {
        ElasticConnection conn = new ElasticConnection();
        RestHighLevelClient client = conn.client;

        SearchRequest searchRequest = new SearchRequest(conn.INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);


        for (SearchHit hit : searchResponse.getHits()) {
            System.out.println(hit.getSourceAsString());
        }


        client.close();
    }
}
