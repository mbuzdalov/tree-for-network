package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Combinatorics;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

public final class BestBSTOverPermutation {
    private final int[][] weightMatrix;
    private final long[][] weightsOut;
    private final int[][] roots;
    private final long[][] costs;

    public BestBSTOverPermutation(int maxN) {
        weightMatrix = new int[maxN][maxN];
        weightsOut = new long[maxN][];
        roots = new int[maxN][];
        costs = new long[maxN][];
        for (int i = 0; i < maxN; ++i) {
            weightsOut[i] = new long[i];
            roots[i] = new int[i + 1];
            roots[i][i] = i;
            costs[i] = new long[i + 1];
        }
    }

    private void fillWeightMatrix(Graph weights, int[] order) {
        int n = weights.nVertices();

        int[] inverse = new int[n];
        Combinatorics.fillInverseOrder(order, inverse);

        // Construct the weight matrix
        for (int i = 0; i < n; ++i) {
            int pi = inverse[i];
            Arrays.fill(weightMatrix[pi], 0, n, 0);
            int nAdj = weights.degree(i);
            for (int j = 0; j < nAdj; ++j) {
                int t = weights.getDestination(i, j);
                int pt = inverse[t];
                weightMatrix[pi][pt] += weights.getWeight(i, j);
            }
        }
    }

    private static long sum(int[] array, int from, int until) {
        long sum = 0;
        for (int i = from; i < until; ++i) {
            sum += array[i];
        }
        return sum;
    }

    private boolean fillWeightsOut(int n, int minChanged, BooleanSupplier timerInterrupt) {
        int perfCounter = 0;
        for (int r = minChanged; r < n; ++r) {
            if (perfCounter > 1000000) {
                perfCounter = 0;
                if (timerInterrupt.getAsBoolean()) {
                    return true;
                }
            }
            weightsOut[r] = new long[r + 1];
            long sum = 0;
            for (int l = r; l >= 0; --l) {
                sum += sum(weightMatrix[l], 0, l);
                sum -= sum(weightMatrix[l], l + 1, r + 1);
                sum += sum(weightMatrix[l], r + 1, n);
                weightsOut[r][l] = sum;
                perfCounter += n;
            }
        }
        return false;
    }

    private static void reconstruct(int[][] roots, int[] order, int l, int r, BoundedForest tree) {
        int m = roots[r][l];
        if (l < m) {
            reconstruct(roots, order, l, m - 1, tree);
            tree.addEdge(order[m], order[roots[m - 1][l]]);
        }
        if (m < r) {
            reconstruct(roots, order, m + 1, r, tree);
            tree.addEdge(order[m], order[roots[r][m + 1]]);
        }
    }

    public BestTreeAlgorithm.Result construct(Graph weights, int[] order, int minChanged, BooleanSupplier timerInterrupt) {
        if (weights.nVertices() != order.length) {
            throw new IllegalArgumentException("Graph size and order array size do not match");
        }
        int n = weights.nVertices();
        if (n > weightMatrix.length) {
            throw new IllegalArgumentException("Graph size is too big (" + n + " vs " + weightMatrix.length + ")");
        }
        fillWeightMatrix(weights, order);
        if (fillWeightsOut(n, minChanged, timerInterrupt)) {
            return null;
        }

        int perfCounter = 0;
        for (int span = 1; span < n; ++span) {
            if (perfCounter > 1000000) {
                perfCounter = 0;
                if (timerInterrupt.getAsBoolean()) {
                    return null;
                }
            }
            for (int r = Math.max(minChanged, span); r < n; ++r) {
                int l = r - span;

                int root = -1;
                long cost = Long.MAX_VALUE;

                for (int m = l; m <= r; ++m) {
                    long costM = 0;
                    if (m > l) costM += costs[m - 1][l] + weightsOut[m - 1][l];
                    if (r > m) costM += costs[r][m + 1] + weightsOut[r][m + 1];
                    if (cost > costM) {
                        cost = costM;
                        root = m;
                    }
                }
                perfCounter += span + 1;

                roots[r][l] = root;
                costs[r][l] = cost;
            }
        }

        BoundedForest forest = new BoundedForest(n);
        reconstruct(roots, order, 0, n - 1, forest);
        return new BestTreeAlgorithm.Result(costs[n - 1][0], forest);
    }
}
