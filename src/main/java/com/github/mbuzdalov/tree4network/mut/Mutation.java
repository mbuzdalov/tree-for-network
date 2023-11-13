package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;

import java.util.Random;

public interface Mutation {
    String getName();

    BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights,
                                    CostComputationAlgorithm costAlgo, Random random);
}
