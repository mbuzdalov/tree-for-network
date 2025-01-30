package com.github.anonymized.tree4network.cost;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;

public final class DefaultCostComputationAlgorithm extends CostComputationAlgorithm {
    private final RMQCostComputationAlgorithm rmq;

    public DefaultCostComputationAlgorithm(int maxSize) {
        this.rmq = new RMQCostComputationAlgorithm(maxSize);
    }

    @Override
    public long compute(Graph weights, BoundedForest tree) {
        long nWeightEdges = 0;
        for (int i = 0; i < weights.nVertices(); ++i) {
            nWeightEdges += weights.degree(i);
        }
        long v2 = (long) (weights.nVertices()) * weights.nVertices();
        if (nWeightEdges >= v2) {
            return NaiveCostComputationAlgorithm.getInstance().compute(weights, tree);
        } else {
            return rmq.compute(weights, tree);
        }
    }
}
