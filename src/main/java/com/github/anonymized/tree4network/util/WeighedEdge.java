package com.github.anonymized.tree4network.util;

public record WeighedEdge(int v1, int v2, int weight) implements Comparable<WeighedEdge> {
    @Override
    public int compareTo(WeighedEdge o) {
        return Integer.compare(weight, o.weight);
    }
}
