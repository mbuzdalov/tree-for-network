package com.github.mbuzdalov.tree4network;

import org.junit.Assert;
import org.junit.Test;

public class BoundedForestTests {
    @Test
    public void testEqualsAndHashCode() {
        BoundedForest f1 = new BoundedForest(3);
        BoundedForest f2 = new BoundedForest(3);

        f1.addEdge(0, 1);
        f1.addEdge(0, 2);

        f2.addEdge(0, 2);
        f2.addEdge(0, 1);

        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f2, f1);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());

        BoundedForest f3 = new BoundedForest(3);
        f3.addEdge(0, 1);
        f3.addEdge(1, 2);

        Assert.assertNotEquals(f1, f3);
        Assert.assertNotEquals(f2, f3);
        Assert.assertNotEquals(f3, f1);
        Assert.assertNotEquals(f3, f2);
    }
}
