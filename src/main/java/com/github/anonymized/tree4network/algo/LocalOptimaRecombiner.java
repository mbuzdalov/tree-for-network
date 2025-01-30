package com.github.anonymized.tree4network.algo;

import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.cost.CostComputationAlgorithm;
import com.github.anonymized.tree4network.cost.DefaultCostComputationAlgorithm;
import com.github.anonymized.tree4network.mut.Mutation;
import com.github.anonymized.tree4network.util.Timer;
import com.github.anonymized.tree4network.xover.Crossover;

import java.util.random.RandomGenerator;

public final class LocalOptimaRecombiner<C1, C2> implements BestTreeAlgorithm {
    private final BestTreeAlgorithm initializer;
    private final Mutation<C1> mutation;
    private final Crossover<C2> crossover;

    public LocalOptimaRecombiner(BestTreeAlgorithm initializer, Mutation<C1> mutation, Crossover<C2> crossover) {
        this.initializer = initializer;
        this.mutation = mutation;
        this.crossover = crossover;
    }

    @Override
    public String getName() {
        return "Simple local search (init = " + initializer.getName() + ", mutation = " + mutation.getName()
                + ", crossover = " + crossover.getName() + ")";
    }

    @Override
    public ResultSupplier construct(Graph weights) {
        return new ResultSupplier() {
            private final CostComputationAlgorithm costAlgo = new DefaultCostComputationAlgorithm(weights.nVertices());
            private final ResultSupplier initialSolutions = initializer.construct(weights);
            private Result bestKnownResult = null;
            private Result lastResult = null;
            private final C1 mutationCtx = mutation.createContext(weights);
            private final C2 crossoverCtx = crossover.createContext(weights);

            @Override
            public Result next(Timer timer, RandomGenerator random) {
                if (lastResult == null) {
                    lastResult = initialSolutions.next(timer, random);
                    mutation.resetContext(mutationCtx);
                    return lastResult;
                } else {
                    if (timer.shouldInterrupt()) {
                        return null;
                    }
                    Result nextResult = mutation.mutate(lastResult, weights, mutationCtx, costAlgo, random, timer);
                    if (nextResult == null) {
                        // try to process the last result with crossover
                        if (bestKnownResult == null) {
                            bestKnownResult = lastResult;
                            // we cannot do much, just restarting
                            lastResult = null;
                            return next(timer, random);
                        } else {
                            // run the crossover between the best result and the last result
                            Result crossoverResult = crossover.crossover(bestKnownResult, lastResult, weights, crossoverCtx, costAlgo, random, timer);
                            // update the best result with the last one after we are done with the crossover
                            if (bestKnownResult.cost() > lastResult.cost()) {
                                bestKnownResult = lastResult;
                            }
                            // if the crossover is even better, we update to its result and return it explicitly,
                            // and also allow the mutation to run more on it
                            if (bestKnownResult.cost() > crossoverResult.cost()) {
                                bestKnownResult = crossoverResult;
                                lastResult = crossoverResult;
                                mutation.resetContext(mutationCtx);
                                return bestKnownResult;
                            } else {
                                // just restart
                                lastResult = null;
                                return next(timer, random);
                            }
                        }
                    } else {
                        if (lastResult.cost() > nextResult.cost()) {
                            lastResult = nextResult;
                            mutation.resetContext(mutationCtx);
                        }
                        return nextResult;
                    }
                }
            }
        };
    }
}
