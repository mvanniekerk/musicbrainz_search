package Search;

import Database.ElasticConnection;
import Database.SearchDB;
import Tokenizer.Tokenizer;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@AllArgsConstructor
public class Result {
    @Getter
    private final List<Work> works = new ArrayList<>();

    @Getter
    int took;

    static Result fromElastic(String resultString) {
        JsonNode result = JacksonSerializer.getInstance().readTree(resultString);
        int took = result.get("took").intValue();

        Result res = new Result(took);

        JsonNode resultList = result.get("hits").get("hits");
        for (JsonNode work : resultList) {
            res.works.add(Work.fromElastic(work));
        }

        return res;
    }

    public static void main(String[] args) {
        String resultString =
                ElasticConnection.getInstance().search("beethoven cello sonata 3", "", "", 0, 50);

        Result result = Result.fromElastic(resultString);
        System.out.println(JacksonSerializer.getInstance().writeAsString(result));

        ElasticConnection.getInstance().close();
    }
}
