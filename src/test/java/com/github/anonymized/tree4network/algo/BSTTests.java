package com.github.anonymized.tree4network.algo;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.GraphBuilder;
import com.github.anonymized.tree4network.cost.NaiveCostComputationAlgorithm;
import com.github.anonymized.tree4network.util.Combinatorics;
import com.github.anonymized.tree4network.util.Timer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class BSTTests {
    private void test(BestBSTOverPermutation solver, Graph g, long expected, int... order) {
        BestTreeAlgorithm.Result result = solver.construct(g, order, 0, Timer.dummyTimer());
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result.cost());
        BoundedForest tree = result.tree();
        Assert.assertEquals(expected, NaiveCostComputationAlgorithm.getInstance().compute(g, tree));
    }

    @Test
    public void singleEdgeBOP() {
        BestBSTOverPermutation solver = new BestBSTOverPermutation(2);
        Graph graph = new GraphBuilder()
                .addEdge(0, 1, 20)
                .result();
        BestTreeAlgorithm.Result fwd = solver.construct(graph, new int[] {0, 1}, 0, Timer.dummyTimer());
        BestTreeAlgorithm.Result bwd = solver.construct(graph, new int[] {0, 1}, 0, Timer.dummyTimer());

        Assert.assertNotNull(fwd);
        Assert.assertNotNull(bwd);
        Assert.assertEquals(20, fwd.cost());
        Assert.assertEquals(20, bwd.cost());

        Assert.assertEquals(1, fwd.tree().degree(0));
        Assert.assertEquals(1, fwd.tree().getDestination(0, 0));
        Assert.assertEquals(1, fwd.tree().degree(1));
        Assert.assertEquals(0, fwd.tree().getDestination(1, 0));

        Assert.assertEquals(1, bwd.tree().degree(0));
        Assert.assertEquals(1, bwd.tree().getDestination(0, 0));
        Assert.assertEquals(1, bwd.tree().degree(1));
        Assert.assertEquals(0, bwd.tree().getDestination(1, 0));
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

    @Test
    public void permutationShortcutCorrectness() {
        int n = 7;
        Random random = new Random(23545424323211L);
        GraphBuilder gb = new GraphBuilder();
        for (int l = 0; l < n; ++l) {
            for (int r = l + 1; r < n; ++r) {
                gb.addEdge(l, r, random.nextInt(10000));
            }
        }
        Graph g = gb.result();

        BestBSTOverPermutation solver1 = new BestBSTOverPermutation(8);
        BestBSTOverPermutation solver2 = new BestBSTOverPermutation(8);

        int[] permutation = new int[n];
        for (int i = 0; i < n; ++i) {
            permutation[i] = i;
        }

        int minChanged = 0;
        do {
            BestTreeAlgorithm.Result r1 = solver1.construct(g, permutation, 0, Timer.dummyTimer());
            BestTreeAlgorithm.Result r2 = solver2.construct(g, permutation, minChanged, Timer.dummyTimer());
            Assert.assertNotNull(r1);
            Assert.assertNotNull(r2);
            Assert.assertEquals(r1.cost(), r2.cost());
        } while ((minChanged = Combinatorics.nextPermutation(permutation)) >= 0);
    }
}
