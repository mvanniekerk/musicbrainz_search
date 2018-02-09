package Search;

import Database.ElasticConnection;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class SearchController {
    public static void main(String[] args) {
        //staticFileLocation("/public");
        // a little hack that auto reloads the static files when they are changed. In production just use
        // staticFileLocation
        staticFiles.externalLocation(System.getProperty("user.dir") + "/src/main/resources/public");


        post("/api", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            res.type("application/json");

            String json = req.body();
            JsonNode result = JacksonSerializer.getInstance().readTree(json);
            String query = result.get("query").textValue();
            int from = result.get("from").intValue();

            return ElasticConnection.getInstance().search(query);
        });
    }

    static boolean isInteger(String string) {
        return string.matches("^-?\\d+$");
    }
}
