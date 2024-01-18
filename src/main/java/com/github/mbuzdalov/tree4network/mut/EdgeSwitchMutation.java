package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Combinatorics;
import com.github.mbuzdalov.tree4network.util.Edge;
import com.github.mbuzdalov.tree4network.util.Graphs;

import java.util.Arrays;
import java.util.Random;

public final class EdgeSwitchMutation implements Mutation<EdgeSwitchMutation.Context> {
    private static final EdgeSwitchMutation INSTANCE = new EdgeSwitchMutation();
    private EdgeSwitchMutation() {}

    public static EdgeSwitchMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Edge switch";
    }

    @Override
    public Context createContext(Graph weights) {
        return new Context(weights.nVertices());
    }

    @Override
    public void resetContext(Context context) {
        context.used = 0;
    }

    @Override
    public BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights, Context context,
                                           CostComputationAlgorithm costAlgo, Random random) {
        if (result.tree().nVertices() <= 2) {
            return null; // nothing to mutate
        }
        BoundedForest tree = new BoundedForest(result.tree());

        if (context.used == 0) {
            Combinatorics.fillRandomPermutation(context.mutations, random);
        }

        // Choose a random edge
        int flippedEdge = context.getMutation(random);
        if (flippedEdge == -1) {
            return null;
        }
        Edge flippedEdgeV = Graphs.getNthEdge(tree, flippedEdge);
        int v1 = flippedEdgeV.v1();
        int v2 = flippedEdgeV.v2();

        tree.removeEdge(v1, v2);
        context.markReachable(tree, v1);
        tree.addEdge(v1, v2);

        // Remove other edges incident to v1
        int[] v1Other = new int[2];
        int n1Other = 0;
        for (int i = tree.degree(v1); --i >= 0; ) {
            int other = tree.getDestination(v1, i);
            if (other != v2) {
                v1Other[n1Other++] = other;
            }
        }
        for (int i = 0; i < n1Other; ++i) {
            tree.removeEdge(v1, v1Other[i]);
        }

        // Remove other edges incident to v2
        int[] v2Other = new int[2];
        int n2Other = 0;
        for (int i = tree.degree(v2); --i >= 0; ) {
            int other = tree.getDestination(v2, i);
            if (other != v1) {
                v2Other[n2Other++] = other;
            }
        }
        for (int i = 0; i < n2Other; ++i) {
            tree.removeEdge(v2, v2Other[i]);
        }

        // Connect them to opposite vertices
        for (int i = 0; i < n1Other; ++i) {
            tree.addEdge(v2, v1Other[i]);
        }
        for (int i = 0; i < n2Other; ++i) {
            tree.addEdge(v1, v2Other[i]);
        }

        long newCost = result.cost();
        int wDeg1 = weights.degree(v1);
        for (int i = 0; i < wDeg1; ++i) {
            int u1 = weights.getDestination(v1, i);
            if (u1 != v2) {
                int w = weights.getWeight(v1, i);
                if (context.isMarked(u1)) {
                    newCost += w;
                } else {
                    newCost -= w;
                }
            }
        }
        int wDeg2 = weights.degree(v2);
        for (int i = 0; i < wDeg2; ++i) {
            int u2 = weights.getDestination(v2, i);
            if (u2 != v1) {
                int w = weights.getWeight(v2, i);
                if (context.isMarked(u2)) {
                    newCost -= w;
                } else {
                    newCost += w;
                }
            }
        }

        // Okay, this is our new tree
        return new BestTreeAlgorithm.Result(newCost, tree);
    }

    public static class Context {
        private Context(int n) {
            mutations = new int[n - 1];
            visited = new boolean[n];
        }
        private final int[] mutations;
        private final boolean[] visited;
        private int used;

        private void markReachable(BoundedForest tree, int v) {
            Arrays.fill(visited, false);
            visited[v] = true;
            dfs(tree, v);
        }

        private boolean isMarked(int v) {
            return visited[v];
        }

        private void dfs(BoundedForest tree, int v) {
            int nAdj = tree.degree(v);
            for (int i = 0; i < nAdj; ++i) {
                int next = tree.getDestination(v, i);
                if (!visited[next]) {
                    visited[next] = true;
                    dfs(tree, next);
                }
            }
        }

        private int getMutation(Random random) {
            int firstUsed = mutations.length - used;
            if (firstUsed == 0) {
                return -1;
            }
            int index = random.nextInt(firstUsed);
            int result = mutations[index];
            ++used;
            --firstUsed;
            mutations[index] = mutations[firstUsed];
            mutations[firstUsed] = result;
            return result;
        }
    }
}
