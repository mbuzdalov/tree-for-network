package com.github.mbuzdalov.tree4network;

import java.util.Arrays;

public final class Util {
    public static boolean isTree(Graph g) {
        // a graph is a tree if it has V-1 edges and is connected
        int v = g.nVertices();
        int nEdges = 0;
        for (int i = 0; i < v; ++i) {
            nEdges += g.nAdjacentVertices(i);
        }
        if (nEdges != 2 * (v - 1)) {
            return false;
        }
        boolean[] visited = new boolean[v];
        int[] stack = new int[v];
        int size = 1;
        visited[0] = true;
        int nVisited = 1;
        while (size > 0) {
            int curr = stack[--size];
            int nAdj = g.nAdjacentVertices(curr);
            for (int i = 0; i < nAdj; ++i) {
                int next = g.getDestination(curr, i);
                if (!visited[next]) {
                    visited[next] = true;
                    ++nVisited;
                    stack[size] = next;
                    ++size;
                }
            }
        }
        return nVisited == v;
    }

    public static long computeCost(Graph weights, Graph tree) {
        if (weights.nVertices() != tree.nVertices()) {
            throw new IllegalArgumentException("Graph sizes do not match");
        }
        int n = weights.nVertices();
        // This is slow, need something faster
        int[][] treeDist = new int[n][n];
        int[] queue = new int[n];

        for (int src = 0; src < n; ++src) {
            int[] distArray = treeDist[src];
            Arrays.fill(distArray, n);
            distArray[src] = 0;
            int head = 0, tail = 0;
            queue[head++] = src;
            while (head > tail) {
                int curr = queue[tail++];
                int nextDist = distArray[curr] + 1;
                int nAdj = tree.nAdjacentVertices(curr);
                for (int i = 0; i < nAdj; ++i) {
                    int next = tree.getDestination(curr, i);
                    if (distArray[next] > nextDist) {
                        distArray[next] = nextDist;
                        queue[head++] = next;
                    }
                }
            }
        }

        long result = 0;
        for (int src = 0; src < n; ++src) {
            int nAdj = weights.nAdjacentVertices(src);
            for (int i = 0; i < nAdj; ++i) {
                int dst = weights.getDestination(src, i);
                result += (long) (weights.getWeight(src, i)) * treeDist[src][dst];
            }
        }

        return result / 2;
    }
}
