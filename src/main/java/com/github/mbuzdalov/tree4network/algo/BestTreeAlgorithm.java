package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.function.BiConsumer;

public interface BestTreeAlgorithm {
    record Result(long cost, BoundedForest tree) {}
    interface ResultSupplier {
        Result next(Timer timer);
    }

    String getName();
    ResultSupplier construct(Graph weights);

    default Result solve(Graph weights, Timer timer, BiConsumer<Long, Long> logger) {
        ResultSupplier supplier = construct(weights);
        Result best = null;
        int nQueries = 0;
        long bestCost = Long.MAX_VALUE;
        while (true) {
            Result curr = supplier.next(timer);
            if (curr == null) {
                System.out.println("  [debug] successful queries: " + nQueries);
                return best;
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
