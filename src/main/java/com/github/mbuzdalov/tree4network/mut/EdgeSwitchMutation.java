package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Combinatorics;

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
        int n = tree.nVertices();

        if (context.used == 0) {
            Combinatorics.fillRandomPermutation(context.mutations, random);
        }

        // Choose a random edge
        int flippedEdge = context.getMutation(random);
        if (flippedEdge == -1) {
            return null;
        }
        int v1 = 0;
        while (tree.degree(v1) <= flippedEdge) {
            flippedEdge -= tree.degree(v1);
            ++v1;
        }
        int v2 = tree.getDestination(v1, flippedEdge);

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

        // Okay, this is our new tree
        return new BestTreeAlgorithm.Result(costAlgo.compute(weights, tree), tree);
    }

    public static class Context {
        private Context(int n) {
            mutations = new int[n];
        }
        private final int[] mutations;
        private int used;

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
