package Scoring;

import Database.ElasticConnection;
import Search.Result;
import Search.Work;

import java.io.IOException;
import java.util.List;

abstract class Scorer {
    abstract double calculateScore(TestCase[] testCases) throws IOException;

    List<Work> search(TestCase testCase, int numResults) throws IOException {
        String resultString =
                ElasticConnection
                        .getInstance()
                        .search(testCase.getQuery(), "", "", 0, numResults);

        Result result = Result.fromElastic(resultString);

        return result.getLeaves();
    }
}
