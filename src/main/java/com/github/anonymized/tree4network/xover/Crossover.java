package com.github.anonymized.tree4network.xover;

import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.algo.BestTreeAlgorithm;
import com.github.anonymized.tree4network.cost.CostComputationAlgorithm;
import com.github.anonymized.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public interface Crossover<Context> {
    String getName();

    Context createContext(Graph weights);

    void resetContext(Context context);

    BestTreeAlgorithm.Result crossover(BestTreeAlgorithm.Result resultA, BestTreeAlgorithm.Result resultB,
                                       Graph weights, Context context, CostComputationAlgorithm costAlgo,
                                       RandomGenerator random, Timer timer);
}
