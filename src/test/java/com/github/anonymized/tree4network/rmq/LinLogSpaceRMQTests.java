package com.github.anonymized.tree4network.rmq;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

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
            int[] array = new int[size];
            NaiveRMQ naive = new NaiveRMQ(array);
            LinLogSpaceRMQ log = new LinLogSpaceRMQ(array);
            do {
                naive.reloadArray(size);
                log.reloadArray(size);
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

    @Test
    public void torture() {
        Random random = new Random(141353423);
        int maxSize = 50;
        int[] array = new int[maxSize];
        NaiveRMQ naive = new NaiveRMQ(array);
        LinLogSpaceRMQ log = new LinLogSpaceRMQ(array);
        for (int run = 0; run < 1000; ++run) {
            int size = run == 0 ? maxSize : 2 + random.nextInt(maxSize - 1);
            for (int i = 0; i < size; ++i) {
                array[i] = random.nextInt(10000);
            }
            naive.reloadArray(size);
            log.reloadArray(size);
            for (int l = 0; l < size; ++l) {
                for (int r = l + 1; r <= size; ++r) {
                    int ei = naive.minimumIndex(l, r);
                    int fi = log.minimumIndex(l, r);
                    int ev = array[ei];
                    int fv = array[fi];
                    if (ev != fv) {
                        Assert.fail("Array: " + Arrays.toString(array) + ", l = " + l + ", r = " + r
                                + ", expected " + ei + " => " + ev + ", found " + fi + " => " + fv);
                    }
                }
            }
        }
    }
}
