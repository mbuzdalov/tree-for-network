package com.github.mbuzdalov.tree4network;

import com.github.mbuzdalov.tree4network.cost.NaiveCostComputationAlgorithm;

public final class Util {
    public static long computeCost(Graph weights, BoundedForest tree) {
        return NaiveCostComputationAlgorithm.getInstance().compute(weights, tree);
    }
}
