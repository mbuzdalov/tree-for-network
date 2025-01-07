package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Edge;

import java.util.random.RandomGenerator;

public final class SubtreeSwapMutation implements Mutation<SubtreeSwapMutation.Context> {
    private static final SubtreeSwapMutation INSTANCE = new SubtreeSwapMutation();
    private SubtreeSwapMutation() {}

    public static SubtreeSwapMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Subtree swap";
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
            return null;
        }

        BoundedForest tree = new BoundedForest(result.tree());

        Edge mutation;
        do {
            mutation = context.getMutation(random);
        } while (mutation != null && tree.hasEdge(mutation.v1(), mutation.v2()));

        if (mutation == null) {
            return null;
        }

        swapSubtrees(tree, mutation.v1(), mutation.v2());
        return new BestTreeAlgorithm.Result(costAlgo.compute(weights, tree), tree);
    }

    private static void swapSubtrees(BoundedForest tree, int v1, int v2) {
        int d = tree.degree(v1);
        for (int i = 0; i < d; ++i) {
            int next = tree.getDestination(v1, i);
            int dfsCall = dfs(tree, next, v1, v2);
            if (dfsCall != -1) {
                tree.removeEdge(dfsCall, v2);
                tree.removeEdge(v1, next);
                tree.addEdge(v1, dfsCall);
                tree.addEdge(v2, next);
                return;
            }
        }
        throw new AssertionError();
    }

    private static int dfs(BoundedForest tree, int vertex, int parent, int stopVertex) {
        int d = tree.degree(vertex);
        for (int i = 0; i < d; ++i) {
            int next = tree.getDestination(vertex, i);
            if (next != parent) {
                if (next == stopVertex) {
                    return vertex;
                }
                int dfsCall = dfs(tree, next, vertex, stopVertex);
                if (dfsCall != -1) {
                    return dfsCall;
                }
            }
        }
        return -1;
    }

    public static final class Context {
        private final Edge[] mutations;
        private final int nVertices;
        private int used;

        private Context(int nVertices) {
            this.nVertices = nVertices;
            if (nVertices <= 1000) {
                mutations = new Edge[nVertices * (nVertices - 1) / 2];
                for (int v1 = 0, i = 0; v1 < nVertices; ++v1) {
                    for (int v2 = v1 + 1; v2 < nVertices; ++v2, ++i) {
                        mutations[i] = new Edge(v1, v2);
                    }
                }
            } else {
                mutations = null;
            }
        }

        private Edge getMutation(RandomGenerator random) {
            if (mutations == null) {
                int v1 = random.nextInt(nVertices);
                int v2 = random.nextInt(nVertices - 1);
                return v1 > v2 ? new Edge(v2, v1) : new Edge(v1, v2 + 1);
            } else {
                int firstUsed = mutations.length - used;
                if (firstUsed == 0) {
                    return null;
                }
                int index = random.nextInt(firstUsed);
                Edge result = mutations[index];
                ++used;
                --firstUsed;
                mutations[index] = mutations[firstUsed];
                mutations[firstUsed] = result;
                return result;
            }
        }
    }
}
