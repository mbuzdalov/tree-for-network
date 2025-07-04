package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.function.BiConsumer;
import java.util.random.RandomGenerator;

public interface BestTreeAlgorithm {
    record Result(long cost, BoundedSimpleGraph tree) {}
    record ExtendedResult(Result result, long nQueries) {}

    interface ResultSupplier {
        Result next(Timer timer, RandomGenerator random);
    }

    String getName();
    ResultSupplier construct(Graph weights, int maxDegree);

    default ExtendedResult solve(Graph weights, int maxDegree,
                                 Timer timer, RandomGenerator random, BiConsumer<Long, Long> logger) {
        ResultSupplier supplier = construct(weights, maxDegree);
        Result best = null;
        long nQueries = 0;
        long bestCost = Long.MAX_VALUE;
        while (true) {
            Result curr = supplier.next(timer, random);
            if (curr == null) {
                return new ExtendedResult(best, nQueries);
            }
            if (curr.cost < bestCost) {
                bestCost = curr.cost;
                logger.accept(timer.timeConsumedMillis(), bestCost);
            }
            ++nQueries;
            if (best == null || best.cost > curr.cost) {
                best = curr;
            }
        }
    }
}
