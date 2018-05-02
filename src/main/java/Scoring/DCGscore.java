package Scoring;

import Database.ElasticConnection;
import Search.Result;
import Search.Work;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DCGscore implements Scorer {
    private int numResults;


    double calculateScore(TestCase testCase) {
        String resultString =
                ElasticConnection
                        .getInstance()
                        .search(testCase.getQuery(), "", "", 0, numResults);

        Result result = Result.fromElastic(resultString);

        List<Work> resultList = result.getLeaves();
//        System.out.println(resultList
//                .stream()
//                .map(a -> a.getNames().get(0))
//                .reduce("", (str, s) -> str + "; " + s)
//        );

        double score = 0;

        int i = 1;
        for (Work work : resultList) {
            if (work.getGid().equals(testCase.getExpected())) {
                double dcg = 1 / (Math.log(i + 1) / Math.log(2));
                score += dcg;
            }
            i++;
        }

        return score;
    }

    public double calculateScore(TestCase[] testCases) {
        double i = 0, sum = 0;
        for (TestCase testCase : testCases) {
            double score = calculateScore(testCase);
            System.out.println(score + ", " + testCase.getQuery() + ", " + testCase.getExpected());
            sum += score;
            i++;
        }
        return sum / i;
    }
}
