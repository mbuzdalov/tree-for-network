package com.github.mbuzdalov.tree4network.xover;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
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
    public Context createContext(Graph weights, int maxDegree) {
        return new Context(weights, maxDegree);
    }

    @Override
    public void resetContext(Context context) {
        context.reset();
    }

    @Override
    public BestTreeAlgorithm.Result crossover(BestTreeAlgorithm.Result resultA, BestTreeAlgorithm.Result resultB,
                                              Context context, CostComputationAlgorithm costAlgo,
                                              RandomGenerator random, Timer timer) {
        context.reset();
        context.load(resultA.tree());
        context.load(resultB.tree());
        int n = context.weights.nVertices();
        BoundedSimpleGraph tree = context.generate(n, random);

        long cost = costAlgo.compute(context.weights, tree);
        context.consume(resultA.cost(), resultB.cost(), cost);
        return new BestTreeAlgorithm.Result(cost, tree);
    }

    public static class Context {
        private final Graph weights;
        private final int[] edgeStart, edgeEnd;
        private final DisjointSet ds;
        private final int maximumDegree;
        private int nEdges;

        private final int[] counts = new int[4];
        private long bestCrossover = Long.MAX_VALUE;

        private void consume(long oldCostA, long oldCostB, long newCost) {
            bestCrossover = Math.min(bestCrossover, newCost);
            if (newCost < oldCostA && newCost < oldCostB) {
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
            System.out.println("Counts: " + Arrays.toString(counts) + ", best crossover: " + bestCrossover);
        }

        private Context(Graph weights, int maximumDegree) {
            this.weights = weights;
            int n = weights.nVertices();
            this.maximumDegree = maximumDegree;
            edgeStart = new int[2 * n];
            edgeEnd = new int[2 * n];
            ds = new DisjointSet(n);
        }

        private void reset() {
            nEdges = 0;
            ds.reset();
        }

        private void load(BoundedSimpleGraph graph) {
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

        private BoundedSimpleGraph generate(int n, RandomGenerator random) {
            BoundedSimpleGraph result;
            int baseNEdges = nEdges;
            int attempts = MAX_RESAMPLE_ATTEMPTS;
            do {
                result = new BoundedSimpleGraph(n, maximumDegree);
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

        private void sample(RandomGenerator random, BoundedSimpleGraph target) {
            int index = random.nextInt(nEdges);
            int src = edgeStart[index];
            int dst = edgeEnd[index];
            --nEdges;
            edgeStart[index] = edgeStart[nEdges];
            edgeEnd[index] = edgeEnd[nEdges];
            if (ds.get(src) != ds.get(dst) && target.degree(src) < maximumDegree && target.degree(dst) < maximumDegree) {
                ds.unite(src, dst);
                target.addEdge(src, dst);
            }
        }

        private void finish(RandomGenerator random, BoundedSimpleGraph target) {
            int n = target.nVertices();
            while (target.nEdges() + 1 < n) {
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                if (ds.get(a) != ds.get(b) && target.degree(a) < maximumDegree && target.degree(b) < maximumDegree) {
                    target.addEdge(a, b);
                    ds.unite(a, b);
                }
            }
        }
    }
}
