package com.github.anonymized.tree4network;

import org.junit.Assert;
import org.junit.Test;

public class GraphBuilderTests {
    @Test
    public void zeroVertices() {
        Graph g = new GraphBuilder().result();
        Assert.assertEquals(0, g.nVertices());
    }

    @Test
    public void singleEdge() {
        Graph g = new GraphBuilder().addEdge(0, 1, 239) .result();

        Assert.assertEquals(2, g.nVertices());
        Assert.assertEquals(1, g.degree(0));
        Assert.assertEquals(1, g.degree(1));
        Assert.assertEquals(1, g.getDestination(0, 0));
        Assert.assertEquals(239, g.getWeight(0, 0));
        Assert.assertEquals(0, g.getDestination(1, 0));
        Assert.assertEquals(239, g.getWeight(1, 0));
    }
}
