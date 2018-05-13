package Scoring;

import Database.CrossFieldSearcher;
import Database.Searcher;
import Search.Result;
import Search.Work;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
abstract class Scorer {
    @Setter
    private Searcher searcher = new CrossFieldSearcher();
    boolean printEachTestCase;
    int numResults;

    abstract double calculateScore(TestCase[] testCases) throws IOException;

    List<Work> search(TestCase testCase, int numResults) throws IOException {
        String resultString = searcher
                .search(testCase.getQuery(), "", "", 0, numResults);

        Result result = Result.fromElastic(resultString);

        return result.getLeaves();
    }
}
