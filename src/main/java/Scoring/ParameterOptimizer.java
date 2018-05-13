package Scoring;

import lombok.Setter;

public abstract class ParameterOptimizer {
    @Setter
    Scoring scoring;

    abstract void optimize() throws InterruptedException;
}
