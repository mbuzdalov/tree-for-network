package com.github.mbuzdalov.tree4network.rmq;

public final class LinLogSpaceRMQ extends RangeMinimumQuery {
    private final int[] values;
    private final int[][] data;
    public LinLogSpaceRMQ(int[] values) {
        this.values = values;
        int logSize = 0;
        while ((1 << logSize) <= values.length) {
            ++logSize;
        }
        --logSize;
        data = new int[logSize][];
        for (int i = 0; i < logSize; ++i) {
            data[i] = new int[values.length - (1 << (i + 1)) + 1];
        }
    }

    @Override
    public void reloadArray(int n) {
        if (data.length > 0) {
            int[] d0 = data[0];
            for (int i = 0; i < d0.length; ++i) {
                d0[i] = values[i] <= values[i + 1] ? i : i + 1;
            }
            for (int d = 1; d < data.length; ++d) {
                int[] curr = data[d];
                int[] prev = data[d - 1];
                int offset = 1 << d;
                for (int i = 0; i < curr.length; ++i) {
                    int lq = prev[i];
                    int rq = prev[i + offset];
                    curr[i] = values[lq] < values[rq] ? lq : rq;
                }
            }
        }
    }

    @Override
    public int minimumIndex(int from, int until) {
        int diff = until - from;
        if (diff == 1) {
            return from;
        }
        int logSize = 30 - Integer.numberOfLeadingZeros(diff);
        int[] a = data[logSize];
        int gapSize = 1 << (logSize + 1);
        if (diff == gapSize) {
            return a[from];
        } else {
            int lq = a[from];
            int rq = a[from + diff - gapSize];
            return values[lq] < values[rq] ? lq : rq;
        }
    }
}
