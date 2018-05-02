package Scoring;

import Database.ElasticConnection;
import Search.Result;
import Search.Work;

import java.util.List;

abstract class Scorer {
    abstract double calculateScore(TestCase[] testCases);

    List<Work> search(TestCase testCase, int numResults) {
        String resultString =
                ElasticConnection
                        .getInstance()
                        .search(testCase.getQuery(), "", "", 0, numResults);

        Result result = Result.fromElastic(resultString);

        return result.getLeaves();
    }
}
