package com.github.mbuzdalov.tree4network.util;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class Graphs {
    private Graphs() {}

    public static void shuffle(WeighedEdge[] edges, RandomGenerator random) {
        int last = 0;
        int n = edges.length;
        for (int i = 1; i < n; ++i) {
            if (edges[last].compareTo(edges[i]) != 0) {
                Combinatorics.shufflePart(edges, last, i, random);
                last = i;
            }
        }
        Combinatorics.shufflePart(edges, last, n, random);
    }

    public static Edge getNthEdge(BoundedSimpleGraph tree, int index) {
        for (int v1 = 0; v1 < tree.nVertices(); ++v1) {
            int d = tree.degree(v1);
            for (int i = 0; i < d; ++i) {
                int v2 = tree.getDestination(v1, i);
                if (v1 < v2) {
                    if (index == 0) {
                        return new Edge(v1, v2);
                    }
                    --index;
                }
            }
        }
        throw new IllegalArgumentException("index is outside of range");
    }

    public static class OptimalRelink {
        private final Graph weights;
        private final int maxDegree;
        private final int[] queue;
        private final int[] components, representatives;
        private final long[] sumWeights;
        private final long[] subtreeSum;
        private BoundedSimpleGraph forest;
        private long bestAnswer;
        private int bestIndex;

        public OptimalRelink(Graph weights, int maxDegree) {
            this.weights = weights;
            this.maxDegree = maxDegree;
            int maxSize = weights.nVertices();

            queue = new int[maxSize];
            components = new int[maxSize];
            representatives = new int[2];
            sumWeights = new long[maxSize];
            subtreeSum = new long[maxSize];

            initializeSumWeights();
        }

        public Edge solve(BoundedSimpleGraph forest) {
            this.forest = forest;

            int nComponents = markComponents();
            if (nComponents != 2) {
                throw new AssertionError("A tree without one edge has to have two components");
            }

            // Compute the optimal answer
            initializeSumWeights();
            computeSubtree(representatives[0], -1);
            computeSubtree(representatives[1], -1);
            int newV1 = computeAnswer(representatives[0]);
            int newV2 = computeAnswer(representatives[1]);

            this.forest = null;
            return new Edge(newV1, newV2);
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

        private void initializeSumWeights() {
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
            if (forest.degree(vertex) < maxDegree && (bestIndex == -1 || bestAnswer > answer)) {
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
