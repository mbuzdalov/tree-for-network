package com.github.anonymized.tree4network.cost;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;

import java.util.Arrays;

public final class NaiveCostComputationAlgorithm extends CostComputationAlgorithm {
    private static final CostComputationAlgorithm INSTANCE = new NaiveCostComputationAlgorithm();

    private NaiveCostComputationAlgorithm() {}

    public static CostComputationAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public long compute(Graph weights, BoundedForest tree) {
        if (weights.nVertices() != tree.nVertices()) {
            throw new IllegalArgumentException("Graph sizes do not match");
        }

        // This is slow, need something faster
        int n = weights.nVertices();
        int[] distArray = new int[n];
        int[] queue = new int[n];

        long result = 0;
        for (int src = 0; src < n; ++src) {
            int nAdj = weights.degree(src);
            if (nAdj == 0) {
                continue;
            }

            Arrays.fill(distArray, n);
            distArray[src] = 0;
            int head = 0, tail = 0;
            queue[head++] = src;
            while (head > tail) {
                int curr = queue[tail++];
                int nextDist = distArray[curr] + 1;
                int currAdj = tree.degree(curr);
                for (int i = 0; i < currAdj; ++i) {
                    int next = tree.getDestination(curr, i);
                    if (distArray[next] > nextDist) {
                        distArray[next] = nextDist;
                        queue[head++] = next;
                    }
                }
            }

            for (int i = 0; i < nAdj; ++i) {
                int dst = weights.getDestination(src, i);
                result += (long) (weights.getWeight(src, i)) * distArray[dst];
            }
        }

        return result / 2;
    }
}
