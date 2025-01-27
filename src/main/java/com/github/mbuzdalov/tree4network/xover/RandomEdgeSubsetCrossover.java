package com.github.mbuzdalov.tree4network.xover;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.DisjointSet;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class RandomEdgeSubsetCrossover implements Crossover<RandomEdgeSubsetCrossover.Context> {
    private static final RandomEdgeSubsetCrossover INSTANCE = new RandomEdgeSubsetCrossover();
    private static final int MAX_RESAMPLE_ATTEMPTS = 10; // these do not seem to help much for BST
    private RandomEdgeSubsetCrossover() {}

    public static RandomEdgeSubsetCrossover getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "random edge subset";
    }

    @Override
    public Context createContext(Graph weights) {
        return new Context(weights.nVertices());
    }

    @Override
    public void resetContext(Context context) {
        context.reset();
    }

    @Override
    public BestTreeAlgorithm.Result crossover(BestTreeAlgorithm.Result resultA, BestTreeAlgorithm.Result resultB,
                                              Graph weights, Context context, CostComputationAlgorithm costAlgo,
                                              RandomGenerator random, Timer timer) {
        context.reset();
        context.load(resultA.tree());
        context.load(resultB.tree());
        int n = weights.nVertices();
        BoundedForest tree = context.generate(n, random);

        long cost = costAlgo.compute(weights, tree);
        context.consume(resultA.cost(), resultB.cost(), cost);
        return new BestTreeAlgorithm.Result(cost, tree);
    }

    public static class Context {
        private final int[] edgeStart, edgeEnd;
        private final DisjointSet ds;
        private int nEdges;

        private final int[] counts = new int[4];
        private final long[] bestImprovement1 = new long[2];
        private final long[] bestImprovement2 = new long[2];

        private void consume(long oldCostA, long oldCostB, long newCost) {
            if (newCost < oldCostA && newCost < oldCostB) {
                long pCost = Math.min(oldCostA, oldCostB);
                System.out.println("Improved from " + pCost + " to " + newCost);
                if (bestImprovement1[0] == 0 || newCost < bestImprovement1[0]) {
                    bestImprovement1[0] = newCost;
                    bestImprovement1[1] = pCost;
                }
                if (pCost - newCost > bestImprovement2[1] - bestImprovement2[0]) {
                    bestImprovement2[0] = newCost;
                    bestImprovement2[1] = pCost;
                }
                counts[0]++;
            } else if (newCost == oldCostA || newCost == oldCostB) {
                System.out.println("Repeated best of the parents");
                counts[1]++;
            } else if (newCost > oldCostA && newCost > oldCostB) {
                System.out.println("Worse than both parents");
                counts[3]++;
            } else {
                System.out.println("Strictly between the parents");
                counts[2]++;
            }
            System.out.println("Counts: " + Arrays.toString(counts) + ", best improvement: " + bestImprovement1[1] + " => " + bestImprovement1[0]
                    + ", deepest improvement: " + bestImprovement2[1] + " => " + bestImprovement2[0]);
        }

        private Context(int n) {
            edgeStart = new int[2 * n];
            edgeEnd = new int[2 * n];
            ds = new DisjointSet(n);
        }

        private void reset() {
            nEdges = 0;
            ds.reset();
        }

        private void load(BoundedForest graph) {
            int n = graph.nVertices();
            for (int v = 0; v < n; ++v) {
                int d = graph.degree(v);
                for (int e = 0; e < d; ++e) {
                    int w = graph.getDestination(v, e);
                    if (v < w) {
                        edgeStart[nEdges] = v;
                        edgeEnd[nEdges] = w;
                        ++nEdges;
                    }
                }
            }
        }

        private BoundedForest generate(int n, RandomGenerator random) {
            BoundedForest result;
            int baseNEdges = nEdges;
            int attempts = MAX_RESAMPLE_ATTEMPTS;
            do {
                result = new BoundedForest(n);
                ds.reset();
                // Phase 1: sample random edges from the parents
                while (result.nEdges() + 1 < n && nEdges > 0) {
                    sample(random, result);
                }
                if (result.nEdges() + 1 == n) {
                    return result;
                }
                nEdges = baseNEdges;
            } while (--attempts > 0);
            finish(random, result);
            return result;
        }

        private void sample(RandomGenerator random, BoundedForest target) {
            int index = random.nextInt(nEdges);
            int src = edgeStart[index];
            int dst = edgeEnd[index];
            --nEdges;
            edgeStart[index] = edgeStart[nEdges];
            edgeEnd[index] = edgeEnd[nEdges];
            if (ds.get(src) != ds.get(dst) && target.degree(src) < 3 && target.degree(dst) < 3) {
                ds.unite(src, dst);
                target.addEdge(src, dst);
            }
        }

        private void finish(RandomGenerator random, BoundedForest target) {
            int n = target.nVertices();
            while (target.nEdges() + 1 < n) {
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                if (ds.get(a) != ds.get(b) && target.degree(a) < 3 && target.degree(b) < 3) {
                    target.addEdge(a, b);
                    ds.unite(a, b);
                }
            }
        }
    }
}
