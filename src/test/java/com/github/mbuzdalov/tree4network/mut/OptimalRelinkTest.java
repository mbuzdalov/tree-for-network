package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;
import com.github.mbuzdalov.tree4network.util.Edge;
import com.github.mbuzdalov.tree4network.util.Graphs;
import org.junit.Assert;
import org.junit.Test;

public class OptimalRelinkTest {
    @Test
    public void smallTest() {
        Graph weights = new GraphBuilder()
                .addEdge(1, 2, 10)
                .result();

        BoundedForest initialTree = new BoundedForest(4);
        initialTree.addEdge(0, 1);
        initialTree.addEdge(0, 2);
        initialTree.addEdge(2, 3);

        BoundedForest expectedTree = new BoundedForest(4);
        expectedTree.addEdge(0, 1);
        expectedTree.addEdge(1, 2);
        expectedTree.addEdge(2, 3);

        initialTree.removeEdge(0, 2);
        Edge best = new Graphs.OptimalRelink(4).solve(initialTree, weights);
        initialTree.addEdge(best.v1(), best.v2());

        Assert.assertEquals(expectedTree, initialTree);
    }
}
