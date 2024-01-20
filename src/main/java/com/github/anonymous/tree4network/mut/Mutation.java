package com.github.anonymous.tree4network.mut;

import com.github.anonymous.tree4network.Graph;
import com.github.anonymous.tree4network.algo.BestTreeAlgorithm;
import com.github.anonymous.tree4network.cost.CostComputationAlgorithm;

import java.util.Random;

public interface Mutation<Context> {
    String getName();

    Context createContext(Graph weights);

    void resetContext(Context context);

    BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights, Context context,
                                    CostComputationAlgorithm costAlgo, Random random);
}
