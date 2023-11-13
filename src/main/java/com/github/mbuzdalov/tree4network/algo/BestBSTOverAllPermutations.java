package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;

import java.util.function.BooleanSupplier;

public final class BestBSTOverAllPermutations implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best BST over all permutations";
    }

    @Override
    public Result construct(Graph weights, long timeLimitMillis) {
        long timeStartMillis = System.currentTimeMillis();
        int n = weights.nVertices();
        BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
        Result bestResult = null;

        BooleanSupplier timerInterrupt = () -> System.currentTimeMillis() - timeStartMillis > timeLimitMillis;

        int minChanged = 0;
        int[] vertexOrder = new int[n];
        for (int i = 0; i < n; ++i) {
            vertexOrder[i] = i;
        }

        int nQueriesCompleted = 0;
        do {
            Result currResult = solver.construct(weights, vertexOrder, minChanged, timerInterrupt);
            if (currResult == null) {
                break;
            }
            ++nQueriesCompleted;
            if (bestResult == null || bestResult.cost() > currResult.cost()) {
                bestResult = currResult;
            }
        } while ((minChanged = Combinatorics.nextPermutation(vertexOrder)) >= 0
                && System.currentTimeMillis() - timeStartMillis < timeLimitMillis);

        System.out.println("  [debug] completed queries: " + nQueriesCompleted);
        return bestResult;
    }
}
