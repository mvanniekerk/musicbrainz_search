package Scoring;

import Database.ElasticConnection;
import Database.CrossFieldSearcher;
import Database.Searcher;
import lombok.Getter;

import java.io.IOException;
import java.util.Random;

public class Scoring {
    @SuppressWarnings("nullness")
    @Getter
    private TestCase[] testCases;
    private Scorer scorer;
    private Loader loader;
    @Getter
    private Searcher searcher;

    private ParameterOptimizer optimizer;

    Scoring setLoader(Loader loader) {
        this.loader = loader;
        return this;
    }

    Scoring setSearcher(Searcher searcher) {
        this.searcher = searcher;
        return this;
    }

    Scoring setScorer(Scorer scorer) {
        this.scorer = scorer;
        scorer.setScoring(this);
        return this;
    }

    Scoring setParameterOptimizer(ParameterOptimizer optimizer) {
        this.optimizer = optimizer;
        optimizer.setScoring(this);
        return this;
    }

    double safeCalculateScore() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            try {
                return calculateScore();
            } catch (Exception e) {
                if (i == 4) {
                    System.out.println("Tried 5 times...");
                    throw new RuntimeException(e);
                } else {
                    Thread.sleep(1000);
                }
            }
        }
        throw new RuntimeException("Unreachable");
    }

    double calculateScore() throws IOException {
        return scorer.calculateScore(testCases);
    }

    void loadTestCases() throws Exception {
        testCases = loader.loadTestCases();
    }

    void optimize() throws InterruptedException {
        optimizer.optimize();
    }



    public static void run() throws Exception {
        double seed = new Random().nextDouble();

        Scoring scoring = new Scoring()
                // .setLoader(new SqlArtistLoader(200, seed))
                .setLoader(new FileLoader("testCases.json"))
                .setScorer(new DcgScore(false, 20))
                .setSearcher(new CrossFieldSearcher(2,1,2))
                .setParameterOptimizer(new GridSearchParameterOptimizer(0.6, 3, 0.4, 0, 1, 0.2))
                ;

        scoring.loadTestCases();
        scoring.optimize();

        scoring.setParameterOptimizer(new BoostParameterOptimizer(1, 3, 0.25f));
        scoring.optimize();

        System.out.println("Seed: " + seed);

        ElasticConnection.getInstance().close();
    }

    public static void scoringRun() throws Exception {
        double seed = new Random().nextDouble();

        Scoring scoring = new Scoring()
                // .setLoader(new SqlArtistLoader(200, seed))
                .setLoader(new FileLoader("testCases.json"))
                .setScorer(new DcgScore(true, 20))
                .setSearcher(new CrossFieldSearcher(2,1,2))
                ;

        scoring.loadTestCases();
        System.out.println(scoring.calculateScore());
        System.out.println("Seed: " + seed);
    }

    public static void main(String[] args) throws Exception {
        Scoring.run();
    }
}
