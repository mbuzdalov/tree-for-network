package com.github.mbuzdalov.tree4network.cost;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;
import com.github.mbuzdalov.tree4network.util.DisjointSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class CostTortureTest {
    @Test
    public void costTortureTest() {
        int maxV = 1000;
        int maxE = 10000;
        int maxD = 3;
        CostComputationAlgorithm naive = NaiveCostComputationAlgorithm.getInstance();
        CostComputationAlgorithm rmq = new RMQCostComputationAlgorithm(maxV);

        Random random = new Random(2354543532424L);

        for (int run = 0; run < 10; ++run) {
            int v = maxV / 2 + random.nextInt(maxV - maxV / 2 + 1);
            int e = maxE / 2 + random.nextInt(maxE - maxE / 2 + 1);
            GraphBuilder builder = new GraphBuilder();
            for (int i = 0; i < e; ++i) {
                int v1, v2;
                do {
                    v1 = random.nextInt(v);
                    v2 = random.nextInt(v);
                } while (v1 == v2);
                builder.addEdge(v1, v2, random.nextInt(1000));
            }
            Graph g = builder.result();
            BoundedSimpleGraph tree = new BoundedSimpleGraph(v, maxD);
            DisjointSet ds = new DisjointSet(v);
            while (tree.nEdges() + 1 < v) {
                int v1, v2;
                do {
                    v1 = random.nextInt(v);
                    v2 = random.nextInt(v);
                } while (tree.degree(v1) == 3 || tree.degree(v2) == 3 || ds.get(v1) == ds.get(v2));
                ds.unite(v1, v2);
                tree.addEdge(v1, v2);
            }

            long naiveCost = naive.compute(g, tree);
            long rmqCost = rmq.compute(g, tree);
            Assert.assertEquals(naiveCost, rmqCost);
        }
    }
}
