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
    public ResultSupplier construct(Graph weights) {
        return new ResultSupplier() {
            private final int n = weights.nVertices();
            private final BestBSTOverPermutation solver = new BestBSTOverPermutation(n);
            private final int[] vertexOrder = new int[n];
            private boolean firstTime = true, dead = false;

            @Override
            public Result next(Timer timer) {
                dead |= timer.shouldInterrupt();
                if (dead) {
                    return null;
                }
                int minChanged;
                if (firstTime) {
                    minChanged = 0;
                    firstTime = false;
                    for (int i = 0; i < n; ++i) {
                        vertexOrder[i] = i;
                    }
                } else {
                    minChanged = n;
                    int perfCounter = 0;
                    while ((minChanged = Math.min(minChanged, Combinatorics.nextPermutation(vertexOrder))) >= 0) {
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
                    if (minChanged == -1) {
                        dead = true;
                        return null;
                    }
                }
                Result currResult = solver.construct(weights, vertexOrder, minChanged, timer);
                dead = currResult == null;
                return currResult;
            }
        };
    }
}
