package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;

import java.util.Arrays;

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
        for (int i = 0; i < n; ++i) {
            inverse[order[i]] = i;
        }

        // Construct the weight matrix
        for (int i = 0; i < n; ++i) {
            int pi = inverse[i];
            Arrays.fill(weightMatrix[pi], 0, n, 0);
            int nAdj = weights.nAdjacentVertices(i);
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

    private void fillWeightsOut(int n) {
        for (int r = 0; r < n; ++r) {
            weightsOut[r] = new long[r + 1];
            long sum = 0;
            for (int l = r; l >= 0; --l) {
                sum += sum(weightMatrix[l], 0, l);
                sum -= sum(weightMatrix[l], l + 1, r + 1);
                sum += sum(weightMatrix[l], r + 1, n);
                weightsOut[r][l] = sum;
            }
        }
    }

    private static void reconstruct(int[][] roots, int[] order, int l, int r, GraphBuilder builder) {
        int m = roots[r][l];
        if (l < m) {
            reconstruct(roots, order, l, m - 1, builder);
            builder.addEdge(order[m], order[roots[m - 1][l]], 1);
        }
        if (m < r) {
            reconstruct(roots, order, m + 1, r, builder);
            builder.addEdge(order[m], order[roots[r][m + 1]], 1);
        }
    }

    public BestTreeAlgorithm.Result construct(Graph weights, int[] order) {
        if (weights.nVertices() != order.length) {
            throw new IllegalArgumentException("Graph size and order array size do not match");
        }
        int n = weights.nVertices();
        if (n > weightMatrix.length) {
            throw new IllegalArgumentException("Graph size is too big (" + n + " vs " + weightMatrix.length + ")");
        }
        fillWeightMatrix(weights, order);
        fillWeightsOut(n);

        for (int span = 1; span < n; ++span) {
            for (int r = span; r < n; ++r) {
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

                roots[r][l] = root;
                costs[r][l] = cost;
            }
        }

        GraphBuilder builder = new GraphBuilder();
        reconstruct(roots, order, 0, n - 1, builder);
        return new BestTreeAlgorithm.Result(costs[n - 1][0], builder.result());
    }
}
