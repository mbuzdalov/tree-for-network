package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Timer;

public interface BestTreeAlgorithm {
    record Result(long cost, BoundedForest tree) {}
    interface ResultSupplier {
        Result next(Timer timer);
    }

    String getName();
    ResultSupplier construct(Graph weights);

    default Result solve(Graph weights, Timer timer) {
        ResultSupplier supplier = construct(weights);
        Result best = null;
        int nQueries = 0;
        while (true) {
            Result curr = supplier.next(timer);
            if (curr == null) {
                System.out.println("  [debug] successful queries: " + nQueries);
                return best;
            }
            ++nQueries;
            if (best == null || best.cost > curr.cost) {
                best = curr;
            }
        }
    }
}
