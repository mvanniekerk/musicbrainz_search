package Database;

import Aggregation.DataStore.WorkStore;
import Aggregation.dataType.MBWork;
import org.apache.http.HttpHost;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;


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

    public static void main(String[] args) throws Exception {
        ElasticConnection conn = ElasticConnection.getInstance();
        RestHighLevelClient client = conn.client;

        WorkStore works = new WorkStore(10000, 20000);
        works.aggregateFromDB();

        for (MBWork work : works) {
            String json = work.jsonSearchRepr();
            String gid = work.getGid();

            conn.storeDocument(json, gid);
        }
        System.out.println("ready");
        //client.close();
    }
}
