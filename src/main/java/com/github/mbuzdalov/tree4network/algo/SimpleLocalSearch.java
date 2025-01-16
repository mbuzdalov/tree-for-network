package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.cost.DefaultCostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.mut.Mutation;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public final class SimpleLocalSearch<C> implements BestTreeAlgorithm {
    private final BestTreeAlgorithm initializer;
    private final Mutation<C> mutation;

    public SimpleLocalSearch(BestTreeAlgorithm initializer, Mutation<C> mutation) {
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
            private final ResultSupplier initialSolutions = initializer.construct(weights);
            private Result lastResult = null;
            private final C context = mutation.createContext(weights);

            @Override
            public Result next(Timer timer, RandomGenerator random) {
                if (lastResult == null) {
                    lastResult = initialSolutions.next(timer, random);
                    mutation.resetContext(context);
                    return lastResult;
                } else {
                    if (timer.shouldInterrupt()) {
                        return null;
                    }
                    Result nextResult = mutation.mutate(lastResult, weights, context, costAlgo, random, timer);
                    if (nextResult == null) {
                        // the mutation says it is done, need to restart
                        lastResult = null;
                        return next(timer, random);
                    } else {
                        if (lastResult.cost() > nextResult.cost()) {
                            lastResult = nextResult;
                            mutation.resetContext(context);
                        }
                        return nextResult;
                    }
                }
            }
        };
    }
}
