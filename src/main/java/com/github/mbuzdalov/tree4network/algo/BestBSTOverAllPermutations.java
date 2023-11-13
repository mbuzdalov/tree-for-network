package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;
import com.github.mbuzdalov.tree4network.util.Timer;

public final class BestBSTOverAllPermutations implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best BST over all permutations";
    }

    @Override
    public Result construct(Graph weights, Timer timer) {
        int n = weights.nVertices();
        BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
        Result bestResult = null;

        int minChanged = 0;
        int[] vertexOrder = new int[n];
        for (int i = 0; i < n; ++i) {
            vertexOrder[i] = i;
        }

        int nQueriesCompleted = 0;
        int minChangedSinceLastQuery = n;
        do {
            minChangedSinceLastQuery = Math.min(minChangedSinceLastQuery, minChanged);
            if (vertexOrder[0] < vertexOrder[n - 1]) {
                Result currResult = solver.construct(weights, vertexOrder, minChangedSinceLastQuery, timer);
                if (currResult == null) {
                    break;
                }
                minChangedSinceLastQuery = n;
                ++nQueriesCompleted;
                if (bestResult == null || bestResult.cost() > currResult.cost()) {
                    bestResult = currResult;
                }
            }
        } while ((minChanged = Combinatorics.nextPermutation(vertexOrder)) >= 0
                && !timer.shouldInterrupt());

        System.out.println("  [debug] completed queries: " + nQueriesCompleted);
        return bestResult;
    }
}
