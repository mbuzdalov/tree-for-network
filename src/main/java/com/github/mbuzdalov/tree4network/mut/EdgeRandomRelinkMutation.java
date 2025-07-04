package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Edge;
import com.github.mbuzdalov.tree4network.util.Graphs;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class EdgeRandomRelinkMutation implements Mutation<EdgeRandomRelinkMutation.Context> {
    private static final EdgeRandomRelinkMutation INSTANCE = new EdgeRandomRelinkMutation();
    private EdgeRandomRelinkMutation() {}

    public static EdgeRandomRelinkMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Edge random relink";
    }

    @Override
    public Context createContext(Graph weights, int maxDegree) {
        return new Context(weights, maxDegree);
    }

    @Override
    public void resetContext(Context context) {}

    @Override
    public BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Context context,
                                           CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
        if (result.tree().nVertices() <= 2) {
            return null; // nothing to mutate
        }

        BoundedSimpleGraph tree = new BoundedSimpleGraph(result.tree());
        // Choose a random edge
        int flippedEdge = random.nextInt(tree.nEdges());
        Edge flippedEdgeV = Graphs.getNthEdge(tree, flippedEdge);
        int v1 = flippedEdgeV.v1();
        int v2 = flippedEdgeV.v2();

        // Remove that edge
        tree.removeEdge(v1, v2);
        context.forest = tree;
        int nComponents = context.markComponents();
        if (nComponents != 2) {
            throw new AssertionError("A tree without one edge has to have two components");
        }
        context.dropMaxDegreeVertices();

        // Connect with randomly sampled edge
        int newV1, newV2;
        do {
            newV1 = context.components[0][random.nextInt(context.componentSizes[0])];
            newV2 = context.components[1][random.nextInt(context.componentSizes[1])];
        } while (newV1 == v1 && newV2 == v2);

        tree.addEdge(newV1, newV2);
        long cost = costAlgo.compute(context.weights, tree);
        return new BestTreeAlgorithm.Result(cost, tree);
    }

    public static class Context {
        private final Graph weights;
        private final int maxDegree;
        private final int[] color;
        private final int[][] components;
        private final int[] componentSizes;
        private BoundedSimpleGraph forest;

        private Context(Graph weights, int maxDegree) {
            this.weights = weights;
            this.maxDegree = maxDegree;
            int n = weights.nVertices();
            color = new int[n];
            components = new int[2][n];
            componentSizes = new int[2];
        }

        private void fillBFS(int start, int component) {
            int head = 0, tail = 0;
            color[start] = component;
            int[] queue = components[component];
            queue[head++] = start;
            while (head > tail) {
                int curr = queue[tail++];
                int degree = forest.degree(curr);
                for (int i = 0; i < degree; ++i) {
                    int next = forest.getDestination(curr, i);
                    if (color[next] == -1) {
                        color[next] = component;
                        queue[head++] = next;
                    }
                }
            }
            componentSizes[component] = tail;
        }

        private int markComponents() {
            Arrays.fill(color, -1);
            int nComponents = 0;
            for (int i = 0; i < color.length; ++i) {
                if (color[i] == -1) {
                    fillBFS(i, nComponents++);
                }
            }
            return nComponents;
        }

        private void dropMaxDegreeVertices() {
            for (int c = 0; c < 2; ++c) {
                int[] component = components[c];
                int oldSize = componentSizes[c];
                int newSize = 0;
                for (int i = 0; i < oldSize; ++i) {
                    if (forest.degree(component[i]) < maxDegree) {
                        component[newSize++] = component[i];
                    }
                }
                componentSizes[c] = newSize;
            }
        }
    }
}
