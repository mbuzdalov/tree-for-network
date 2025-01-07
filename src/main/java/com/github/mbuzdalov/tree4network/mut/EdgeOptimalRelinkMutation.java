package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Combinatorics;
import com.github.mbuzdalov.tree4network.util.Edge;
import com.github.mbuzdalov.tree4network.util.Graphs;

import java.util.random.RandomGenerator;

public final class EdgeOptimalRelinkMutation implements Mutation<EdgeOptimalRelinkMutation.Context> {
    private static final EdgeOptimalRelinkMutation INSTANCE = new EdgeOptimalRelinkMutation();
    private EdgeOptimalRelinkMutation() {}

    public static EdgeOptimalRelinkMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Edge optimal relink";
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
                                           CostComputationAlgorithm costAlgo, RandomGenerator random) {
        if (result.tree().nVertices() <= 2) {
            return null; // nothing to mutate
        }

        BoundedForest tree = new BoundedForest(result.tree());
        // Choose a random edge
        int flippedEdge = context.getMutation(random);
        if (flippedEdge == -1) {
            return null;
        }
        Edge flippedEdgeV = Graphs.getNthEdge(tree, flippedEdge);
        int v1 = flippedEdgeV.v1();
        int v2 = flippedEdgeV.v2();

        // Remove that edge
        tree.removeEdge(v1, v2);
        Edge bestEdge = context.relink.solve(tree, weights);
        int newV1 = bestEdge.v1();
        int newV2 = bestEdge.v2();
        tree.addEdge(newV1, newV2);
        if (v1 != newV1 || v2 != newV2) {
            long cost = costAlgo.compute(weights, tree);
            if (cost > result.cost()) {
                throw new AssertionError("Relink does not work optimally: existing cost " + result.cost() + ", new cost " + cost);
            }
            return new BestTreeAlgorithm.Result(cost, tree);
        } else {
            return result;
        }
    }

    public static class Context {
        private final int[] mutations;
        private int used;
        private final Graphs.OptimalRelink relink;

        private Context(int n) {
            mutations = new int[n - 1];
            Combinatorics.fillIdentityPermutation(mutations);
            relink = new Graphs.OptimalRelink(n);
        }

        private int getMutation(RandomGenerator random) {
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
