package com.github.mbuzdalov.tree4network.cost;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;
import com.github.mbuzdalov.tree4network.util.DisjointSet;

import java.util.Random;

public class CostPerformanceEvaluation {
    public static void main(String[] args) {
        int v = 1000;
        int d = 3;
        CostComputationAlgorithm naive = NaiveCostComputationAlgorithm.getInstance();
        CostComputationAlgorithm rmq = new RMQCostComputationAlgorithm(v);

        Random random = new Random(2354543532424L);

        for (int e : new int[] { v, 2 * v, 4 * v, 8 * v, 16 * v, 32 * v, 64 * v, 128 * v, 256 * v, 512 * v, 1024 * v, 2048 * v, 4096 * v, 8192 * v}) {
            System.out.println("******************************************************");
            for (int run = 0; run < 5; ++run) {
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
                BoundedSimpleGraph tree = new BoundedSimpleGraph(v, d);
                DisjointSet ds = new DisjointSet(v);
                while (tree.nEdges() + 1 < v) {
                    int v1, v2;
                    do {
                        v1 = random.nextInt(v);
                        v2 = random.nextInt(v);
                    } while (tree.degree(v1) == d || tree.degree(v2) == d || ds.get(v1) == ds.get(v2));
                    ds.unite(v1, v2);
                    tree.addEdge(v1, v2);
                }

                long t0 = System.nanoTime();
                long naiveCost = naive.compute(g, tree);
                long t1 = System.nanoTime();
                long rmqCost = rmq.compute(g, tree);
                long t2 = System.nanoTime();
                if (naiveCost != rmqCost) {
                    throw new AssertionError();
                }
                System.out.println("V = " + v + ", E = " + e + ": ratio = " + ((double) (t1 - t0) / (t2 - t1)));
            }
        }
    }
}
