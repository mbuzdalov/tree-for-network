package com.github.mbuzdalov.tree4network;

import org.junit.Assert;
import org.junit.Test;

public class BoundedSimpleGraphTests {
    @Test
    public void testEqualsAndHashCode() {
        BoundedSimpleGraph f1 = new BoundedSimpleGraph(3, 3);
        BoundedSimpleGraph f2 = new BoundedSimpleGraph(3, 3);

        Assert.assertEquals(3, f1.maximumDegree());
        Assert.assertEquals(3, f2.maximumDegree());

        f1.addEdge(0, 1);
        f1.addEdge(0, 2);

        f2.addEdge(0, 2);
        f2.addEdge(0, 1);

        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f2, f1);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());

        BoundedSimpleGraph f3 = new BoundedSimpleGraph(3, 3);
        f3.addEdge(0, 1);
        f3.addEdge(1, 2);

        Assert.assertNotEquals(f1, f3);
        Assert.assertNotEquals(f2, f3);
        Assert.assertNotEquals(f3, f1);
        Assert.assertNotEquals(f3, f2);
    }
}
