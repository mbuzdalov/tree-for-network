package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public final class BestBSTOverAllPermutations implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best BST over all permutations";
    }

    @Override
    public ResultSupplier construct(Graph weights) {
        return new ResultSupplier() {
            private final int n = weights.nVertices();
            private final BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
            private final int[] vertexOrder = new int[n];
            private boolean dead = false;
            private long iteration = 0;
            private final long maxIterations = n == 1 ? 1 : Combinatorics.factorialOrMaxLong(n) / 2;

            @Override
            public Result next(Timer timer, RandomGenerator random) {
                dead |= timer.shouldInterrupt();
                dead |= iteration == maxIterations;
                if (dead) {
                    return null;
                }
                int minChanged;
                if (iteration == 0) {
                    minChanged = 0;
                    do {
                        Combinatorics.fillRandomPermutation(vertexOrder, random);
                    } while (vertexOrder[0] > vertexOrder[n - 1]);
                } else {
                    minChanged = n;
                    int perfCounter = 0;
                    while (true) {
                        minChanged = Math.min(minChanged, Combinatorics.nextPermutation(vertexOrder));
                        minChanged = Math.max(0, minChanged); // 987...1 to 123...9 is now legal
                        if (vertexOrder[0] < vertexOrder[n - 1]) {
                            break;
                        }
                        perfCounter += vertexOrder.length;
                        if (perfCounter > 1000000) {
                            perfCounter = 0;
                            if (timer.shouldInterrupt()) {
                                dead = true;
                                return null;
                            }
                        }
                    }
                }
                ++iteration;
                Result currResult = solver.construct(weights, vertexOrder, minChanged, timer);
                dead = currResult == null;
                return currResult;
            }
        };
    }
}
