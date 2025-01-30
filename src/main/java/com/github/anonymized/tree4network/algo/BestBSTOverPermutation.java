package com.github.anonymized.tree4network.algo;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.util.Combinatorics;
import com.github.anonymized.tree4network.util.Timer;

import java.util.Arrays;

public final class BestBSTOverPermutation {
    private final int[][] weightMatrix;
    private final long[][] weightsOut;
    private final int[][] roots;
    private final long[][] costs;

    public BestBSTOverPermutation(int maxN) {
        weightMatrix = new int[maxN][maxN];
        weightsOut = new long[maxN][maxN];
        roots = new int[maxN][];
        costs = new long[maxN][maxN];
        for (int i = 0; i < maxN; ++i) {
            roots[i] = new int[i + 1];
            roots[i][i] = i;
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

    private boolean fillWeightsOut(int n, int minChanged, Timer timer) {
        int perfCounter = 0;
        for (int r = minChanged; r < n; ++r) {
            if (perfCounter > 1000000) {
                perfCounter = 0;
                if (timer.shouldInterrupt()) {
                    return true;
                }
            }
            long sum = 0;
            for (int l = r; l >= 0; --l) {
                sum += sum(weightMatrix[l], 0, l);
                sum -= sum(weightMatrix[l], l + 1, r + 1);
                sum += sum(weightMatrix[l], r + 1, n);
                weightsOut[r][l] = sum;
                weightsOut[l][r] = sum;
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

    public BestTreeAlgorithm.Result construct(Graph weights, int[] order, int minChanged, Timer timer) {
        if (weights.nVertices() != order.length) {
            throw new IllegalArgumentException("Graph size and order array size do not match");
        }
        int n = weights.nVertices();
        if (n > weightMatrix.length) {
            throw new IllegalArgumentException("Graph size is too big (" + n + " vs " + weightMatrix.length + ")");
        }
        fillWeightMatrix(weights, order);
        if (fillWeightsOut(n, minChanged, timer)) {
            return null;
        }

        int perfCounter = 0;
        for (int span = 1; span < n; ++span) {
            if (perfCounter > 1000000) {
                perfCounter = 0;
                if (timer.shouldInterrupt()) {
                    return null;
                }
            }
            for (int r = Math.max(minChanged, span); r < n; ++r) {
                int l = r - span;

                int root = -1;
                long cost = Long.MAX_VALUE;

                long[] cl = costs[l], cr = costs[r], wl = weightsOut[l], wr = weightsOut[r];
                for (int m = l; m <= r; ++m) {
                    long costM = 0;
                    if (m > l) costM += cl[m - 1] + wl[m - 1];
                    if (r > m) costM += cr[m + 1] + wr[m + 1];
                    if (cost > costM) {
                        cost = costM;
                        root = m;
                    }
                }
                perfCounter += span + 1;

                roots[r][l] = root;
                costs[r][l] = cost;
                costs[l][r] = cost;
            }
        }

        BoundedForest forest = new BoundedForest(n);
        reconstruct(roots, order, 0, n - 1, forest);
        return new BestTreeAlgorithm.Result(costs[n - 1][0], forest);
    }
}
