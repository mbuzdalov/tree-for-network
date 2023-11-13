package com.github.mbuzdalov.tree4network.rmq;

public abstract class RangeMinimumQuery {
    public abstract void reloadArray(int size);
    public abstract int minimumIndex(int from, int until);
}
