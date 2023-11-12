package com.github.mbuzdalov.tree4network.util;

public final class DisjointSet {
    private final int[] rank;
    private final int[] parent;

    public DisjointSet(int n) {
        rank = new int[n];
        parent = new int[n];
        reset();
    }

    public int get(int index) {
        int p = index;
        while (parent[p] != p) {
            p = parent[p];
        }
        while (index != p) {
            int q = parent[index];
            parent[index] = p;
            index = q;
        }
        return p;
    }

    public void unite(int a, int b) {
        a = get(a);
        b = get(b);
        if (a != b) {
            if (rank[a] == rank[b]) {
                ++rank[a];
            }
            if (rank[a] < rank[b]) {
                parent[a] = b;
            } else {
                parent[b] = a;
            }
        }
    }

    public void reset() {
        for (int i = 0; i < parent.length; ++i) {
            rank[i] = 0;
            parent[i] = i;
        }
    }
}
