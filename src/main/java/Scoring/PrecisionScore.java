package Scoring;

import Search.Work;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class PrecisionScore extends Scorer {
    private int numResults;
    private boolean printEachTestCase;

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
    }

    @Override
    double calculateScore(TestCase[] testCases) throws IOException {
        double truePositives = 0;
        for (TestCase testCase: testCases) {
            if (printEachTestCase) printTestCase(testCase);
            truePositives += calculateScore(testCase);
        }
        return truePositives / (double) testCases.length;
    }
}
