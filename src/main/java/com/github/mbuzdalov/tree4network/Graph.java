package com.github.mbuzdalov.tree4network;

public abstract class Graph {
    public abstract int nVertices();
    public abstract int nAdjacentVertices(int vertex);
    public abstract int getDestination(int source, int index);
    public abstract int getWeight(int source, int index);
}
