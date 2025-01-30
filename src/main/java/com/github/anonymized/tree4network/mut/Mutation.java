package com.github.anonymized.tree4network.mut;

import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.algo.BestTreeAlgorithm;
import com.github.anonymized.tree4network.cost.CostComputationAlgorithm;
import com.github.anonymized.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public interface Mutation<Context> {
    String getName();

    Context createContext(Graph weights);

    void resetContext(Context context);

    BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights, Context context,
                                    CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer);
}
