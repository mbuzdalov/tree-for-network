package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public final class BestBSTOverRandomPermutations implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best BST over random permutations";
    }

    @Override
    public ResultSupplier construct(Graph weights, int maxDegree) {
        if (maxDegree != 3) {
            throw new IllegalArgumentException("This algorithm is not supported for maxDegree = " + maxDegree);
        }
        return new ResultSupplier() {
            private final int n = weights.nVertices();
            private final BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
            private final int[] vertexOrder = new int[n];

            @Override
            public Result next(Timer timer, RandomGenerator random) {
                if (timer.shouldInterrupt()) {
                    return null;
                }
                Combinatorics.fillRandomPermutation(vertexOrder, random);
                return solver.construct(weights, vertexOrder, 0, timer);
            }
        };
    }
}
