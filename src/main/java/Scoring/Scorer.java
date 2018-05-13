package Scoring;

import Database.CrossFieldSearcher;
import Database.Searcher;
import Search.Result;
import Search.Work;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

abstract class Scorer {
    boolean printEachTestCase;
    int numResults;
    @Setter
    private Scoring scoring;

    Scorer(boolean printEachTestCase, int numResults) {
        this.printEachTestCase = printEachTestCase;
        this.numResults = numResults;
    }

    abstract double calculateScore(TestCase[] testCases) throws IOException;

    List<Work> search(TestCase testCase, int numResults) throws IOException {
        String resultString = scoring.getSearcher()
                .search(testCase.getQuery(), "", "", 0, numResults);

        Result result = Result.fromElastic(resultString);

        return result.getLeaves();
    }
}
