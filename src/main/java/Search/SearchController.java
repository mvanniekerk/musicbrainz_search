package Search;

import Database.ElasticConnection;

import static spark.Spark.get;
import static spark.Spark.staticFiles;

public class SearchController {
    public static void main(String[] args) {
        //staticFileLocation("/public");
        // a little hack that auto reloads the static files when they are changed. In production just use
        // staticFileLocation
        staticFiles.externalLocation(System.getProperty("user.dir") + "/src/main/resources/public");


        get("/api/:query", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            res.type("application/json");

            String query = req.params(":query");

            return ElasticConnection.getInstance().search(query);
        });
    }

    static boolean isInteger(String string) {
        return string.matches("^-?\\d+$");
    }
}
