package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;

import java.util.concurrent.ThreadLocalRandom;

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

        do {
            vertexOrder[0] = 0;
            for (int i = 1; i < n; ++i) {
                int j = random.nextInt(i + 1);
                vertexOrder[i] = vertexOrder[j];
                vertexOrder[j] = i;
            }
            BestTreeAlgorithm.Result currResult = solver.construct(weights, vertexOrder);
            if (bestResult == null || bestResult.cost() > currResult.cost()) {
                bestResult = currResult;
            }
        } while (System.currentTimeMillis() - timeStartMillis < timeLimitMillis);

        return bestResult;
    }
}
