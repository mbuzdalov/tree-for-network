package com.github.mbuzdalov.tree4network.algo;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.util.Timer;

public interface BestTreeAlgorithm {
    record Result(long cost, BoundedForest tree) {}

    String getName();
    Result construct(Graph weights, Timer timer);
}
