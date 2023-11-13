package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.cost.DefaultCostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.mut.Mutation;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.concurrent.ThreadLocalRandom;

public final class SimpleLocalSearch implements BestTreeAlgorithm {
    private final BestTreeAlgorithm initializer;
    private final Mutation mutation;

    public SimpleLocalSearch(BestTreeAlgorithm initializer, Mutation mutation) {
        this.initializer = initializer;
        this.mutation = mutation;
    }

    @Override
    public String getName() {
        return "Simple local search (init = " + initializer.getName() + ", mutation = " + mutation.getName() + ")";
    }

    @Override
    public ResultSupplier construct(Graph weights) {
        return new ResultSupplier() {
            private final CostComputationAlgorithm costAlgo = new DefaultCostComputationAlgorithm(weights.nVertices());
            private Result lastResult = null;

            @Override
            public Result next(Timer timer) {
                if (lastResult == null) {
                    lastResult = initializer.construct(weights).next(timer);
                    return lastResult;
                } else {
                    if (timer.shouldInterrupt()) {
                        return null;
                    }
                    Result nextResult = mutation.mutate(lastResult, weights, costAlgo, ThreadLocalRandom.current());
                    if (lastResult.cost() >= nextResult.cost()) {
                        lastResult = nextResult;
                    }
                    return nextResult;
                }
            }
        };
    }
}
