package Search;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;
import static spark.Spark.staticFiles;

public class SearchController {
    public static void main(String[] args) {
        //staticFileLocation("/public");
        // a little hack that auto reloads the static files when they are changed. In production just use
        // staticFileLocation
        staticFiles.externalLocation(System.getProperty("user.dir") + "/src/main/resources/public");


        get("/api/:query/:page", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            res.type("application/json");

            String page = req.params(":page");
            int pageCount = 0;
            if (isInteger(page) && Integer.parseInt(page) >= 0) {
                pageCount = Integer.parseInt(page);
            }
            int start = 20*pageCount;
            int end = 20 + 20*pageCount;

            String query = req.params(":query");
            Result result = new Result();
            result.retrieveQuery(query);
            result.calcTfIdf();
            result.tfIdfOrderedWorkList();
            result.getNames(start, end);
            return result.orderedWorkListAsJson(start, end);
        });
    }

    static boolean isInteger(String string) {
        return string.matches("^-?\\d+$");
    }
}
