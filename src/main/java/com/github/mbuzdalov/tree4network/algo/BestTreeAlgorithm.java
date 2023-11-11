package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.Graph;

public interface BestTreeAlgorithm {
    record Result(long cost, Graph tree) {}

    Result construct(Graph weights, long timeLimitMillis);
}
