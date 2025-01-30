package com.github.anonymized.tree4network.rmq;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class LinSpaceIncrementalRMQTests {
    private void fillArray(int[] array, int mask) {
        int value = 0;
        array[0] = value;
        for (int i = 1; i < array.length; ++i) {
            if ((mask & 1) == 0) {
                --value;
            } else {
                ++value;
            }
            array[i] = value;
            mask >>>= 1;
        }
    }

    @Test
    public void allSmall() {
        for (int size = 1; size <= 14; ++size) {
            int[] array = new int[size];
            LinLogSpaceRMQ log = new LinLogSpaceRMQ(array);
            LinSpaceIncrementalRMQ lin = new LinSpaceIncrementalRMQ(array);

            for (int mask = 0; mask < (1 << (size - 1)); ++mask) {
                fillArray(array, mask);
                log.reloadArray(size);
                lin.reloadArray(size);
                for (int l = 0; l < size; ++l) {
                    for (int r = l + 1; r <= size; ++r) {
                        int ei = log.minimumIndex(l, r);
                        int fi = lin.minimumIndex(l, r);
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

    @Test
    public void torture() {
        Random random = new Random(141353423);
        int maxSize = 200;
        int[] array = new int[maxSize];
        LinLogSpaceRMQ log = new LinLogSpaceRMQ(array);
        LinSpaceIncrementalRMQ lin = new LinSpaceIncrementalRMQ(array);
        for (int run = 0; run < 100; ++run) {
            int size = run == 0 ? maxSize : 2 + random.nextInt(maxSize - 1);
            array[0] = random.nextInt(100000);
            for (int i = 1; i < size; ++i) {
                array[i] = array[i - 1] + 2 * random.nextInt(2) - 1;
            }
            log.reloadArray(size);
            lin.reloadArray(size);
            for (int l = 0; l < size; ++l) {
                for (int r = l + 1; r <= size; ++r) {
                    int ei = log.minimumIndex(l, r);
                    int fi = lin.minimumIndex(l, r);
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
