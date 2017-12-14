package Search;

import static spark.Spark.get;

public class SearchController {
    public static void main(String[] args) {
        get("/api/:query", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            res.type("application/json");

            String page = req.queryParamOrDefault("page", "0");
            int pageCount = 0;
            if (isInteger(page) && Integer.parseInt(page) >= 0) {
                pageCount = Integer.parseInt(page);
            }
            int start = 20*pageCount;
            int end = 20 + 20*pageCount;

            Result result = new Result();
            result.retrieveQuery(req.params(":query"));
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
