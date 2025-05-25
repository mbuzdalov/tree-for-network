package com.github.mbuzdalov.tree4network.cost;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;

public abstract class CostComputationAlgorithm {
    public abstract long compute(Graph weights, BoundedSimpleGraph tree);
}
