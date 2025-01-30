package com.github.anonymized.tree4network.cost;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;

public abstract class CostComputationAlgorithm {
    public abstract long compute(Graph weights, BoundedForest tree);
}
