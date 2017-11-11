package Database;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticSearchClient {

    @Nullable
    private static ElasticSearchClient elasticSearchClient;

    private TransportClient transportClient;

    private ElasticSearchClient() {
        try {
            transportClient = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(""), 9300));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static TransportClient getTransportClient() {
        if (elasticSearchClient == null) {
            elasticSearchClient = new ElasticSearchClient();
        }
        return elasticSearchClient.transportClient;
    }

    public static void main(String[] args) {
        TransportClient client = ElasticSearchClient.getTransportClient();

        String json = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";

        IndexResponse response = client.prepareIndex("twitter", "tweet")
                .setSource(json, XContentType.JSON)
                .get();

        // Index name
        String _index = response.getIndex();
        // Type name
        String _type = response.getType();
        // Document ID (generated or not)
        String _id = response.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        long _version = response.getVersion();
        // status has stored current instance statement.
        RestStatus status = response.status();

        client.close();
    }
}
