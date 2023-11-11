package com.github.mbuzdalov.tree4network;

import org.junit.Assert;
import org.junit.Test;

public class GraphBuilderTests {
    @Test
    public void zeroVertices() {
        GraphBuilder builder = new GraphBuilder();
        Graph g = builder.result();
        Assert.assertEquals(0, g.nVertices());
    }

    @Test
    public void singleEdge() {
        GraphBuilder builder = new GraphBuilder();
        builder.addEdge(0, 1, 239);
        Graph g = builder.result();
        Assert.assertEquals(2, g.nVertices());
        Assert.assertEquals(1, g.nAdjacentVertices(0));
        Assert.assertEquals(1, g.nAdjacentVertices(1));
        Assert.assertEquals(1, g.getDestination(0, 0));
        Assert.assertEquals(239, g.getWeight(0, 0));
        Assert.assertEquals(0, g.getDestination(1, 0));
        Assert.assertEquals(239, g.getWeight(1, 0));
    }
}
