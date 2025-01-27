package com.github.mbuzdalov.tree4network.xover;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public interface Crossover<Context> {
    String getName();

    Context createContext(Graph weights);

    void resetContext(Context context);

    BestTreeAlgorithm.Result crossover(BestTreeAlgorithm.Result resultA, BestTreeAlgorithm.Result resultB,
                                       Graph weights, Context context, CostComputationAlgorithm costAlgo,
                                       RandomGenerator random, Timer timer);
}
