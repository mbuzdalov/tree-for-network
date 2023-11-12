package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.Util;
import com.github.mbuzdalov.tree4network.util.DisjointSet;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class BestMSTOverEdgeShuffle implements BestTreeAlgorithm {
    private record Edge(int source, int destination, int weight) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            return Integer.compare(o.weight, weight);
        }
    }

    @Override
    public String getName() {
        return "Best MST over random edge orderings";
    }

    private static void shufflePart(Edge[] edges, ThreadLocalRandom random, int from, int until) {
        for (int i = from + 1; i < until; ++i) {
            int j = random.nextInt(i - from + 1) + from;
            Edge e = edges[i];
            edges[i] = edges[j];
            edges[j] = e;
        }
    }

    private static void shuffle(Edge[] edges, ThreadLocalRandom random) {
        int last = 0;
        int n = edges.length;
        for (int i = 1; i < n; ++i) {
            if (edges[last].compareTo(edges[i]) != 0) {
                shufflePart(edges, random, last, i);
                last = i;
            }
        }
        shufflePart(edges, random, last, n);
    }

    @Override
    public Result construct(Graph weights, long timeLimitMillis) {
        int n = weights.nVertices();
        long startTimeMillis = System.currentTimeMillis();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        DisjointSet ds = new DisjointSet(n);
        BestTreeAlgorithm.Result bestResult = null;
        int nEdges = 0;
        for (int i = 0; i < n; ++i) {
            nEdges += weights.nAdjacentVertices(i);
        }
        nEdges /= 2;
        int[] degree = new int[n];
        Edge[] edges = new Edge[nEdges];
        for (int i = 0, e = 0; i < n; ++i) {
            int nAdj = weights.nAdjacentVertices(i);
            for (int j = 0; j < nAdj; ++j) {
                int t = weights.getDestination(i, j);
                if (i < t) {
                    edges[e] = new Edge(i, t, weights.getWeight(i, j));
                    ++e;
                }
            }
        }
        Arrays.sort(edges);

        do {
            if (bestResult != null) {
                shuffle(edges, random);
            }
            Arrays.fill(degree, 0);
            BoundedForest tree = new BoundedForest(n);
            ds.reset();
            // first, try adding the existing edges
            for (int i = 0; i < nEdges; ++i) {
                Edge curr = edges[i];
                int src = curr.source;
                int dst = curr.destination;
                if (degree[src] < 3 && degree[dst] < 3 && ds.get(src) != ds.get(dst)) {
                    ++degree[src];
                    ++degree[dst];
                    ds.unite(src, dst);
                    tree.addEdge(src, dst);
                }
            }
            // if they are not enough, add random connectors
            // this is too random but might work
            while (tree.nEdges() + 1 < n) {
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                if (degree[a] < 3 && degree[b] < 3 && ds.get(a) != ds.get(b)) {
                    ++degree[a];
                    ++degree[b];
                    ds.unite(a, b);
                    tree.addEdge(a, b);
                }
            }
            long treeCost = Util.computeCost(weights, tree);
            if (bestResult == null || bestResult.cost() > treeCost) {
                bestResult = new Result(treeCost, tree);
            }
        } while (System.currentTimeMillis() - startTimeMillis < timeLimitMillis);
        return bestResult;
    }
}
