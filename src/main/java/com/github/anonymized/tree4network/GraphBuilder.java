package com.github.anonymized.tree4network;

import java.util.ArrayList;
import java.util.List;

public final class GraphBuilder {
    private record EdgeEnd(int destination, int weight, EdgeEnd next) {}

    private final List<EdgeEnd> lastEnds = new ArrayList<>();

    public GraphBuilder addEdge(int v1, int v2, int weight) {
        if (v1 == v2) {
            throw new IllegalArgumentException("Vertex numbers coincide");
        }
        if (v1 < 0 || v2 < 0) {
            throw new IllegalArgumentException("Vertex number(s) are negative");
        }
        int vMax = Math.max(v1, v2);
        while (lastEnds.size() <= vMax) {
            lastEnds.add(null);
        }
        lastEnds.set(v1, new EdgeEnd(v2, weight, lastEnds.get(v1)));
        lastEnds.set(v2, new EdgeEnd(v1, weight, lastEnds.get(v2)));
        return this;
    }

    private static class ArrayGraph extends Graph {
        private final int[][] data;
        private final int nEdges;
        ArrayGraph(int[][] data) {
            this.data = data;
            int nEdges = 0;
            for (int[] row : data) {
                nEdges += row.length >>> 1;
            }
            this.nEdges = nEdges >>> 1;
        }

        @Override
        public int nVertices() {
            return data.length;
        }

        @Override
        public int nEdges() {
            return nEdges;
        }

        @Override
        public int degree(int vertex) {
            return data[vertex].length >>> 1;
        }

        @Override
        public int getDestination(int source, int index) {
            return data[source][index << 1];
        }

        @Override
        public int getWeight(int source, int index) {
            return data[source][1 + (index << 1)];
        }
    }

    private int[] extractArray(int vertex) {
        int adjacentVertices = 0;
        EdgeEnd e = lastEnds.get(vertex);
        while (e != null) {
            ++adjacentVertices;
            e = e.next;
        }
        int[] result = new int[2 * adjacentVertices];
        e = lastEnds.get(vertex);
        int index = -1;
        while (e != null) {
            result[++index] = e.destination;
            result[++index] = e.weight;
            e = e.next;
        }
        return result;
    }

    public Graph result() {
        int[][] data = new int[lastEnds.size()][];
        for (int i = 0; i < data.length; ++i) {
            data[i] = extractArray(i);
        }
        return new ArrayGraph(data);
    }
}
