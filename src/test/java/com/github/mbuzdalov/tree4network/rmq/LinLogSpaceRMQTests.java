package com.github.mbuzdalov.tree4network.rmq;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class LinLogSpaceRMQTests {
    private boolean next(int[] array) {
        int n = array.length;
        for (int i = 0; i < n; ++i) {
            if (++array[i] < n) {
                Arrays.fill(array, 0, i, 0);
                return true;
            }
        }
        return false;
    }
    @Test
    public void allSmall() {
        for (int size = 1; size <= 6; ++size) {
            NaiveRMQ naive = new NaiveRMQ(size);
            LinLogSpaceRMQ log = new LinLogSpaceRMQ(size);
            int[] array = new int[size];
            do {
                naive.load(array, 0, size);
                log.load(array, 0, size);
                for (int l = 0; l < size; ++l) {
                    for (int r = l + 1; r <= size; ++r) {
                        int nq = naive.minimumIndex(l, r);
                        int lq = log.minimumIndex(l, r);
                        int nv = array[nq];
                        int lv = array[lq];
                        if (nv != lv) {
                            Assert.fail("Array: " + Arrays.toString(array) + ", l = " + l + ", r = " + r
                                    + ", expected " + nq + " => " + nv + ", found " + lq + " => " + lv);
                        }
                    }
                }
            } while (next(array));
        }
    }
}
