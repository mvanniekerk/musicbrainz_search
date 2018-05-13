package Search;

import Database.CrossFieldSearcher;
import Database.ElasticConnection;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;

import java.io.IOException;

import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class SearchController {

    private static final int RESULT_SIZE = 20;

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
            int page = result.get("page").intValue();
            String composerQuery = result.get("composerQuery").textValue();
            String artistQuery = result.get("artistQuery").textValue();
            int from = (page - 1) * RESULT_SIZE;

            String strResult;
            try {
                strResult = new CrossFieldSearcher(1,1,1).
                        search(query, composerQuery, artistQuery, from, 20);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Result treeStyle = Result.fromElastic(strResult);
            return JacksonSerializer.getInstance().writeAsString(treeStyle);
        });
    }

    static boolean isInteger(String string) {
        return string.matches("^-?\\d+$");
    }
}
