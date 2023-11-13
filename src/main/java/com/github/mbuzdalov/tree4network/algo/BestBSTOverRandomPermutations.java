package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.concurrent.ThreadLocalRandom;

public final class BestBSTOverRandomPermutations implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best BST over random permutations";
    }

    @Override
    public Result construct(Graph weights, Timer timer) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int n = weights.nVertices();
        BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
        BestTreeAlgorithm.Result bestResult = null;
        int[] vertexOrder = new int[n];

        int nQueriesCompleted = 0;
        do {
            Combinatorics.fillRandomPermutation(vertexOrder, random);
            BestTreeAlgorithm.Result currResult = solver.construct(weights, vertexOrder, 0, timer);
            if (currResult == null) {
                break;
            }
            ++nQueriesCompleted;
            if (bestResult == null || bestResult.cost() > currResult.cost()) {
                bestResult = currResult;
            }
        } while (!timer.shouldInterrupt());

        System.out.println("  [debug] completed queries: " + nQueriesCompleted);
        return bestResult;
    }
}
