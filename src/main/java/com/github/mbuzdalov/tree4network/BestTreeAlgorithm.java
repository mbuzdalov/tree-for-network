package com.github.mbuzdalov.tree4network;

public interface BestTreeAlgorithm {
    record Result(long cost, Graph tree) {}

    Result construct(Graph weights, long timeLimit);
}
