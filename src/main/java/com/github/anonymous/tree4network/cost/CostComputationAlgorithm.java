package com.github.anonymous.tree4network.cost;

import com.github.anonymous.tree4network.BoundedForest;
import com.github.anonymous.tree4network.Graph;

public abstract class CostComputationAlgorithm {
    public abstract long compute(Graph weights, BoundedForest tree);
}
