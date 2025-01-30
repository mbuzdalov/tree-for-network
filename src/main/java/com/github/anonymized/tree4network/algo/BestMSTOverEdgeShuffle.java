package com.github.anonymized.tree4network.algo;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.cost.CostComputationAlgorithm;
import com.github.anonymized.tree4network.cost.DefaultCostComputationAlgorithm;
import com.github.anonymized.tree4network.util.DisjointSet;
import com.github.anonymized.tree4network.util.Graphs;
import com.github.anonymized.tree4network.util.Timer;
import com.github.anonymized.tree4network.util.WeighedEdge;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class BestMSTOverEdgeShuffle implements BestTreeAlgorithm {
    @Override
    public String getName() {
        return "Best MST over random edge orderings";
    }

    @Override
    public ResultSupplier construct(Graph weights) {
        return new ResultSupplier() {
            private final int n = weights.nVertices();
            private final int e = weights.nEdges();
            private final DisjointSet ds = new DisjointSet(n);
            private final int[] degree = new int[n];
            private final WeighedEdge[] edges = new WeighedEdge[e];
            private final CostComputationAlgorithm costAlgo = new DefaultCostComputationAlgorithm(n);

            {
                for (int i = 0, ei = 0; i < n; ++i) {
                    int nAdj = weights.degree(i);
                    for (int j = 0; j < nAdj; ++j) {
                        int t = weights.getDestination(i, j);
                        if (i < t) {
                            edges[ei] = new WeighedEdge(i, t, weights.getWeight(i, j));
                            ++ei;
                        }
                    }
                }
                Arrays.sort(edges);
            }

            private boolean firstTime = true;

            @Override
            public Result next(Timer timer, RandomGenerator random) {
                if (timer.shouldInterrupt()) {
                    return null;
                }
                if (firstTime) {
                    firstTime = false;
                } else {
                    Graphs.shuffle(edges, random);
                }
                Arrays.fill(degree, 0);
                BoundedForest tree = new BoundedForest(n);
                ds.reset();
                // first, try adding the existing edges, starting from the heaviest one
                for (int i = e - 1; i >= 0; --i) {
                    WeighedEdge curr = edges[i];
                    int src = curr.v1();
                    int dst = curr.v2();
                    if (degree[src] < 3 && degree[dst] < 3 && ds.get(src) != ds.get(dst)) {
                        ++degree[src];
                        ++degree[dst];
                        ds.unite(src, dst);
                        tree.addEdge(src, dst);
                    }
                }
                // if they are not enough, add random connectors
                // this is too random but might work
                while (tree.nEdges() + 1 < n) {
                    int a = random.nextInt(n);
                    int b = random.nextInt(n);
                    if (degree[a] < 3 && degree[b] < 3 && ds.get(a) != ds.get(b)) {
                        ++degree[a];
                        ++degree[b];
                        ds.unite(a, b);
                        tree.addEdge(a, b);
                    }
                }
                long cost = costAlgo.compute(weights, tree);
                return new Result(cost, tree);
            }
        };
    }
}
