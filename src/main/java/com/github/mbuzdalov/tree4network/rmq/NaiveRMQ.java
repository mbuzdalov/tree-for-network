package com.github.mbuzdalov.tree4network.rmq;

public final class NaiveRMQ extends RangeMinimumQuery {
    private final int[] data;

    public NaiveRMQ(int maxSize) {
        data = new int[maxSize];
    }

    @Override
    public void load(int[] array, int from, int until) {
        System.arraycopy(array, from, data, 0, until - from);
    }

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
