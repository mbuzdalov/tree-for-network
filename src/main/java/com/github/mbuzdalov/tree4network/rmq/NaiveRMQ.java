package com.github.mbuzdalov.tree4network.rmq;

public final class NaiveRMQ extends RangeMinimumQuery {
    private final int[] data;

    public NaiveRMQ(int[] underlyingArray) {
        data = underlyingArray;
    }

    @Override
    public void reloadArray(int size) {}

    @Override
    public int minimumIndex(int from, int until) {
        int result = from;
        int value = data[from];
        for (int i = from; ++i < until; ) {
            int curr = data[i];
            if (value > curr) {
                value = curr;
                result = i;
            }
        }
        return result;
    }
}
