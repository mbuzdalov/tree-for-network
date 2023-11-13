package com.github.mbuzdalov.tree4network.rmq;

public abstract class RangeMinimumQuery {
    public abstract void load(int[] array, int from, int until);
    public abstract int minimumIndex(int from, int until);
}
