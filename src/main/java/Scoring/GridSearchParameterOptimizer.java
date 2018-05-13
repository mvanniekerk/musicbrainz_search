package Scoring;


import lombok.AllArgsConstructor;
import lombok.Setter;

import java.io.IOException;

@AllArgsConstructor
public class GridSearchParameterOptimizer extends ParameterOptimizer {
    private double lowerK1;
    private double higherK1;
    private double stepK1;
    private double lowerB;
    private double higherB;
    private double stepB;

    @Override
    void optimize() throws InterruptedException {
        double highestScore = 0;
        double bestK1 = -1;
        double bestB = -1;

        for (double k1 = lowerK1; k1 < higherK1; k1 += stepK1) {
            for (double b = lowerB; b < higherB; b += stepB) {
                try {
                    scoring.updateParameters(k1, b);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Thread.sleep(1000);
                double score = scoring.safeCalculateScore();

                if (score > highestScore) {
                    highestScore = score;
                    bestK1 = k1;
                    bestB = b;
                }
                System.out.println(k1 + " " + b + " " + score);
            }
        }
        System.out.println(bestK1 + " " + bestB + " " + highestScore);
        try {
            scoring.updateParameters(bestK1, bestB);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
