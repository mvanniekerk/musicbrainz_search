package Scoring;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoostParameterOptimizer extends ParameterOptimizer {
    private float lower;
    private float higher;
    private float step;

    @Override
    void optimize() throws InterruptedException {
        float bestArtistParameter = -1;
        float bestComposerParameter = -1;
        float bestNamesParameter = -1;
        double bestScore = -1;

        for (float artist = lower; artist < higher; artist += step) {
            for (float composer = lower; composer < higher; composer += step) {
                for (float name = lower; name < higher; name += step) {
                    scoring.getSearcher().setArtistBoost(artist);
                    scoring.getSearcher().setComposerBoost(composer);
                    scoring.getSearcher().setNamesBoost(name);
                    double score = scoring.safeCalculateScore();
                    if (score > bestScore) {
                        bestArtistParameter = artist;
                        bestComposerParameter = composer;
                        bestNamesParameter = name;
                        bestScore = score;
                    }
                    System.out.println("artist: " + artist + " composer: " + composer + " name: " + name + " score: " + score);

                }
            }
        }
        System.out.println("Best: artist: " + bestArtistParameter + " composer: " + bestComposerParameter + " name: " + bestNamesParameter + " score: " + bestScore);
    }
}
