package Scoring;

import Database.ElasticConnection;
import Database.Searcher;
import Search.Result;
import Search.Work;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DcgScore extends Scorer {

    public DcgScore(boolean printEachTestCase, int numResults) {
        super(printEachTestCase, numResults);
    }


    private double calculateScore(TestCase testCase) throws IOException {
        List<List<Work>> resultList = search(testCase, numResults);

        double score = 0;

        int i = 1;
        for (List<Work> traversal : resultList) {
            if (anyMatch(traversal, testCase.getExpected())) {
                double dcg = 1 / (Math.log(i + 1) / Math.log(2));
                score += dcg;
            }
            i++;
        }

        return score;
    }

    @Override
    public double calculateScore(TestCase[] testCases) throws IOException {
        double i = 0, sum = 0;
        for (TestCase testCase : testCases) {
            double score = calculateScore(testCase);
            if (printEachTestCase)
                System.out.println(score + ", " + testCase.getQuery() + ", " + testCase.getExpected());
            sum += score;
            i++;
        }
        return sum / i;
    }
}
