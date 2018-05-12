package Scoring;

import Database.Searcher;
import Search.Work;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

public class PrecisionScore extends Scorer {


    public PrecisionScore(Searcher searcher, boolean printEachTestCase, int numResults) {
        super(searcher, printEachTestCase, numResults);
    }


    private double calculateScore(TestCase testCase) throws IOException {
        List<Work> resultList = search(testCase, numResults);
        if (resultList.size() == 0) {
            return 0;
        }
        Work head = resultList.get(0);

        if (head.getGid().equals(testCase.getExpected())) {
            return 1;
        } else {
            return 0;
        }
    }

    private void printTestCase(TestCase testCase) throws IOException {
        List<Work> resultList = search(testCase, numResults);

        int position = 1;
        for (Work work : resultList) {
            if (work.getGid().equals(testCase.getExpected())) {
                System.out.println(position + ", " + testCase.getQuery() + ", " + testCase.getExpected());
                return;
            }
            position++;
        }
        System.out.println(-1 + ", " + testCase.getQuery() + ", " + testCase.getExpected());
    }

    @Override
    double calculateScore(TestCase[] testCases) throws IOException {
        double truePositives = 0;
        for (TestCase testCase: testCases) {
            double score = calculateScore(testCase);
            truePositives += score;
            if (printEachTestCase && score != 1) {
                printTestCase(testCase);
            }
        }
        return truePositives / (double) testCases.length;
    }
}
