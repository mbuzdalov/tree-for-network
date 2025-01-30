package com.github.anonymized.tree4network;

public abstract class Graph {
    public abstract int nVertices();
    public abstract int nEdges();
    public abstract int degree(int vertex);
    public abstract int getDestination(int source, int index);
    public abstract int getWeight(int source, int index);
}
