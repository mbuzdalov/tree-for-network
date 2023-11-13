package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

public final class BestBSTOverRandomPermutations implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best BST over random permutations";
    }

    @Override
    public Result construct(Graph weights, long timeLimitMillis) {
        long timeStartMillis = System.currentTimeMillis();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int n = weights.nVertices();
        BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
        BestTreeAlgorithm.Result bestResult = null;
        int[] vertexOrder = new int[n];

        BooleanSupplier timerInterrupt = () -> System.currentTimeMillis() - timeStartMillis > timeLimitMillis;

        int nQueriesCompleted = 0;
        do {
            Combinatorics.fillRandomPermutation(vertexOrder, random);
            BestTreeAlgorithm.Result currResult = solver.construct(weights, vertexOrder, 0, timerInterrupt);
            if (currResult == null) {
                break;
            }
            ++nQueriesCompleted;
            if (bestResult == null || bestResult.cost() > currResult.cost()) {
                bestResult = currResult;
            }
        } while (System.currentTimeMillis() - timeStartMillis < timeLimitMillis);

        System.out.println("  [debug] completed queries: " + nQueriesCompleted);
        return bestResult;
    }
}
