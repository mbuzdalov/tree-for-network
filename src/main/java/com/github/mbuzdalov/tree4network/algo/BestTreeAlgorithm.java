package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;

public interface BestTreeAlgorithm {
    record Result(long cost, BoundedForest tree) {}

    String getName();
    Result construct(Graph weights, long timeLimitMillis);
}
