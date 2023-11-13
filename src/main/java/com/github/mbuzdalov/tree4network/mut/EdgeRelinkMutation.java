package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Combinatorics;

import java.util.Arrays;
import java.util.Random;

public final class EdgeRelinkMutation implements Mutation<EdgeRelinkMutation.Context> {
    private static final EdgeRelinkMutation INSTANCE = new EdgeRelinkMutation();
    private EdgeRelinkMutation() {}

    public static EdgeRelinkMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Edge relink";
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

        if (context.used == 0) {
            Combinatorics.fillRandomPermutation(context.mutations, random);
        }

        while (true) {
            BoundedForest tree = new BoundedForest(result.tree());
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

            // Remove that edge
            tree.removeEdge(v1, v2);
            context.forest = tree;
            int nComponents = context.markComponents();
            if (nComponents != 2) {
                throw new AssertionError("A tree without one edge has to have two components");
            }

            // Compute the optimal answer
            context.initializeSumWeights(weights);
            context.computeSubtree(context.representatives[0], -1);
            context.computeSubtree(context.representatives[1], -1);
            int newV1 = context.computeAnswer(context.representatives[0]);
            int newV2 = context.computeAnswer(context.representatives[1]);
            tree.addEdge(newV1, newV2);
            if (v1 != newV1 || v2 != newV2) {
                long cost = costAlgo.compute(weights, tree);
                if (cost > result.cost()) {
                    throw new AssertionError("Relink does not work optimally: existing cost " + result.cost() + ", new cost " + cost);
                }
                if (cost < result.cost()) {
                    // Okay, this is our new tree
                    return new BestTreeAlgorithm.Result(cost, tree);
                }
            }
        }
    }

    public static class Context {
        private final int[] mutations;
        private final int[] queue;
        private final int[] components, representatives;
        private final int[] sumWeights;
        private final long[] subtreeSum;
        private BoundedForest forest;
        private int used;

        private long bestAnswer;
        private int bestIndex;

        private Context(int n) {
            mutations = new int[n];
            queue = new int[n];
            components = new int[n];
            representatives = new int[2];
            sumWeights = new int[n];
            subtreeSum = new long[n];
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

        private void fillBFS(int start, int component) {
            int head = 0, tail = 0;
            components[start] = component;
            queue[head++] = start;
            while (head > tail) {
                int curr = queue[tail++];
                int degree = forest.degree(curr);
                for (int i = 0; i < degree; ++i) {
                    int next = forest.getDestination(curr, i);
                    if (components[next] == -1) {
                        components[next] = component;
                        queue[head++] = next;
                    }
                }
            }
        }

        private int markComponents() {
            Arrays.fill(components, -1);
            int nComponents = 0;
            for (int i = 0; i < components.length; ++i) {
                if (components[i] == -1) {
                    representatives[nComponents] = i;
                    fillBFS(i, nComponents++);
                }
            }
            return nComponents;
        }

        private void initializeSumWeights(Graph weights) {
            Arrays.fill(sumWeights, 0);
            for (int v1 = 0; v1 < weights.nVertices(); ++v1) {
                int d1 = weights.degree(v1);
                for (int i = 0; i < d1; ++i) {
                    int v2 = weights.getDestination(v1, i);
                    if (components[v1] != components[v2]) {
                        int w = weights.getWeight(v1, i);
                        sumWeights[v1] += w;
                        sumWeights[v2] += w;
                    }
                }
            }
        }

        private void computeSubtree(int vertex, int parent) {
            subtreeSum[vertex] = sumWeights[vertex];
            int d = forest.degree(vertex);
            for (int i = 0; i < d; ++i) {
                int next = forest.getDestination(vertex, i);
                if (next != parent) {
                    computeSubtree(next, vertex);
                    subtreeSum[vertex] += subtreeSum[next];
                }
            }
        }

        private int computeAnswer(int vertex) {
            bestAnswer = 0; // whatever, should have been subtreeIntegral[vertex], but this is all relative
            bestIndex = -1;
            computeAnswerDFS(vertex, -1, 0, 0);
            return bestIndex;
        }

        private void computeAnswerDFS(int vertex, int parent, long answer, long onTop) {
            if (forest.degree(vertex) < 3 && (bestIndex == -1 || bestAnswer > answer)) {
                bestAnswer = answer;
                bestIndex = vertex;
            }
            int d = forest.degree(vertex);
            for (int i = 0; i < d; ++i) {
                int next = forest.getDestination(vertex, i);
                if (next != parent) {
                    long newOnTop = onTop + subtreeSum[vertex] - subtreeSum[next];
                    computeAnswerDFS(next, vertex, answer - subtreeSum[next] + newOnTop, newOnTop);
                }
            }
        }
    }
}
