package com.github.mbuzdalov.tree4network;

import org.junit.Assert;
import org.junit.Test;

public class BSTTests {
    private void test(BestBSTOverPermutation solver, Graph g, long expected, int... order) {
        BestTreeAlgorithm.Result result = solver.construct(g, order);
        Assert.assertEquals(expected, result.cost());
        Graph tree = result.tree();
        Assert.assertTrue(Util.isTree(tree));
        Assert.assertEquals(expected, Util.computeCost(g, tree));
    }

    @Test
    public void singleEdgeBOP() {
        BestBSTOverPermutation solver = new BestBSTOverPermutation(2);
        Graph graph = new GraphBuilder()
                .addEdge(0, 1, 20)
                .result();
        BestTreeAlgorithm.Result fwd = solver.construct(graph, new int[] {0, 1});
        BestTreeAlgorithm.Result bwd = solver.construct(graph, new int[] {0, 1});

        Assert.assertEquals(20, fwd.cost());
        Assert.assertEquals(20, bwd.cost());

        Assert.assertEquals(1, fwd.tree().nAdjacentVertices(0));
        Assert.assertEquals(1, fwd.tree().getDestination(0, 0));
        Assert.assertEquals(1, fwd.tree().getWeight(0, 0));
        Assert.assertEquals(1, fwd.tree().nAdjacentVertices(1));
        Assert.assertEquals(0, fwd.tree().getDestination(1, 0));
        Assert.assertEquals(1, fwd.tree().getWeight(1, 0));

        Assert.assertEquals(1, bwd.tree().nAdjacentVertices(0));
        Assert.assertEquals(1, bwd.tree().getDestination(0, 0));
        Assert.assertEquals(1, bwd.tree().getWeight(0, 0));
        Assert.assertEquals(1, bwd.tree().nAdjacentVertices(1));
        Assert.assertEquals(0, bwd.tree().getDestination(1, 0));
        Assert.assertEquals(1, bwd.tree().getWeight(1, 0));
    }

    @Test
    public void twoEdgesBOP() {
        Graph graph = new GraphBuilder()
                .addEdge(0, 1, 100)
                .addEdge(0, 2, 10)
                .addEdge(1, 2, 1)
                .result();

        BestBSTOverPermutation solver = new BestBSTOverPermutation(3);
        test(solver, graph, 112,  0, 1, 2);
        test(solver, graph, 112,  2, 1, 0);
        test(solver, graph, 112,  0, 2, 1);
        test(solver, graph, 112,  1, 2, 0);
        test(solver, graph, 112,  1, 0, 2);
        test(solver, graph, 112,  2, 0, 1);
    }

    @Test
    public void arbitraryTestN4() {
        BestBSTOverPermutation solver = new BestBSTOverPermutation(4);

        Graph g1 = new GraphBuilder()
                .addEdge(0, 1, 3).addEdge(0, 2, 6).addEdge(0, 3, 7)
                .addEdge(1, 2, 4).addEdge(1, 3, 5).addEdge(2, 3, 8)
                .result();

        test(solver, g1, 48,   0, 1, 2, 3);  test(solver, g1, 48,   3, 2, 1, 0);
        test(solver, g1, 46,   0, 1, 3, 2);  test(solver, g1, 46,   2, 3, 1, 0);
        test(solver, g1, 48,   0, 2, 1, 3);  test(solver, g1, 48,   3, 1, 2, 0);
        test(solver, g1, 46,   0, 2, 3, 1);  test(solver, g1, 46,   1, 3, 2, 0);
        test(solver, g1, 46,   0, 3, 1, 2);  test(solver, g1, 46,   2, 1, 3, 0);
        test(solver, g1, 46,   0, 3, 2, 1);  test(solver, g1, 46,   1, 2, 3, 0);
        test(solver, g1, 48,   1, 0, 2, 3);  test(solver, g1, 48,   3, 2, 0, 1);
        test(solver, g1, 46,   1, 0, 3, 2);  test(solver, g1, 46,   2, 3, 0, 1);
        test(solver, g1, 48,   1, 2, 0, 3);  test(solver, g1, 48,   3, 0, 2, 1);
        test(solver, g1, 46,   1, 3, 0, 2);  test(solver, g1, 46,   2, 0, 3, 1);
        test(solver, g1, 50,   2, 0, 1, 3);  test(solver, g1, 50,   3, 1, 0, 2);
        test(solver, g1, 50,   2, 1, 0, 3);  test(solver, g1, 50,   3, 0, 1, 2);
    }
}
