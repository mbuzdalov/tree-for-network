package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;

import java.util.Random;

public final class EdgeSwitchMutation implements Mutation {
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
    public BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights,
                                           CostComputationAlgorithm costAlgo, Random random) {
        if (result.tree().nVertices() <= 2) {
            return result; // nothing to mutate
        }
        BoundedForest tree = new BoundedForest(result.tree());
        int n = tree.nVertices();

        // Choose a random vertex and a random edge
        int v1 = random.nextInt(n);
        int e12 = random.nextInt(tree.degree(v1));
        int v2 = tree.getDestination(v1, e12);

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
}
