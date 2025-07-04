package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public interface Mutation<Context> {
    String getName();

    Context createContext(Graph weights, int maxDegree);

    void resetContext(Context context);

    BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Context context,
                                    CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer);
}
