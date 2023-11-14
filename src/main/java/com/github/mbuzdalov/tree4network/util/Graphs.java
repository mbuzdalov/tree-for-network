package com.github.mbuzdalov.tree4network.util;

import com.github.mbuzdalov.tree4network.BoundedForest;

public final class Graphs {
    private Graphs() {}

    public record Edge(int v1, int v2) {}

    public static Edge getNthEdge(BoundedForest tree, int index) {
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
}
