package com.github.mbuzdalov.tree4network.rmq;

import org.junit.Assert;
import org.junit.Test;

public class NaiveRMQTests {
    @Test
    public void handmadeTests() {
        //              0  1  2   3  4  5  6  7  8
        int[] array = { 1, 3, 2, -1, 5, 4, 2, 6, 0 };
        RangeMinimumQuery rmq = new NaiveRMQ(array);
        rmq.reloadArray(array.length);

        Assert.assertEquals(3, rmq.minimumIndex(0, 9));
        Assert.assertEquals(0, rmq.minimumIndex(0, 1));
        Assert.assertEquals(1, rmq.minimumIndex(1, 2));
        Assert.assertEquals(0, rmq.minimumIndex(0, 2));
        Assert.assertEquals(8, rmq.minimumIndex(5, 9));
        Assert.assertEquals(6, rmq.minimumIndex(5, 8));

        array[5] = -2;
        rmq.reloadArray(array.length);

        Assert.assertEquals(5, rmq.minimumIndex(0, 9));
        Assert.assertEquals(0, rmq.minimumIndex(0, 1));
        Assert.assertEquals(1, rmq.minimumIndex(1, 2));
        Assert.assertEquals(0, rmq.minimumIndex(0, 2));
        Assert.assertEquals(5, rmq.minimumIndex(5, 9));
        Assert.assertEquals(5, rmq.minimumIndex(5, 8));
    }
}
