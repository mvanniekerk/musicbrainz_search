package Search;

import static spark.Spark.get;

public class SearchController {
    public static void main(String[] args) {
        get("/api/:query", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            res.header("Content-Type", "application/json");

            Result result = new Result();
            result.retrieveQuery(req.params(":query"));
            result.calcTfIdf();
            result.tfIdfOrderedWorkList();
            return result.orderedWorkListAsJson(0, 20);
        });
    }
}
