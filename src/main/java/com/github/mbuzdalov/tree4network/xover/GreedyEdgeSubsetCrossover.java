package com.github.mbuzdalov.tree4network.xover;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.DisjointSet;
import com.github.mbuzdalov.tree4network.util.Graphs;
import com.github.mbuzdalov.tree4network.util.Timer;
import com.github.mbuzdalov.tree4network.util.WeighedEdge;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class GreedyEdgeSubsetCrossover implements Crossover<GreedyEdgeSubsetCrossover.Context> {
    private static final GreedyEdgeSubsetCrossover INSTANCE = new GreedyEdgeSubsetCrossover();
    private GreedyEdgeSubsetCrossover() {}

    public static GreedyEdgeSubsetCrossover getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "greedy edge subset";
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
        Graphs.shuffle(context.edges, random);
        int n = context.weights.nVertices();
        int d = context.maximumDegree;
        BoundedSimpleGraph tree = new BoundedSimpleGraph(n, d);

        BoundedSimpleGraph a = resultA.tree();
        BoundedSimpleGraph b = resultB.tree();
        DisjointSet ds = context.ds;
        ds.reset();

        // Phase 1: link the edges which are in the graphs
        for (int ei = context.edges.length - 1; ei >= 0; --ei) {
            WeighedEdge e = context.edges[ei];
            int v1 = e.v1();
            int v2 = e.v2();
            if (tree.degree(v1) < d && tree.degree(v2) < d && ds.get(v1) != ds.get(v2) && (a.hasEdge(v1, v2) || b.hasEdge(v1, v2))) {
                ds.unite(v1, v2);
                tree.addEdge(v1, v2);
            }
        }
        // Phase 2: link the edges which are not in the graphs, if needed
        for (int ei = context.edges.length - 1; ei >= 0; --ei) {
            WeighedEdge e = context.edges[ei];
            int v1 = e.v1();
            int v2 = e.v2();
            if (tree.degree(v1) < d && tree.degree(v2) < d && ds.get(v1) != ds.get(v2)) {
                ds.unite(v1, v2);
                tree.addEdge(v1, v2);
            }
        }
        // Phase 3: add extra edges if needed
        while (tree.nEdges() + 1 < n) {
            int v1 = random.nextInt(n);
            int v2 = random.nextInt(n);
            if (tree.degree(v1) < d && tree.degree(v2) < d && ds.get(v1) != ds.get(v2)) {
                ds.unite(v1, v2);
                tree.addEdge(v1, v2);
            }
        }

        long cost = costAlgo.compute(context.weights, tree);
        context.consume(resultA.cost(), resultB.cost(), cost);
        return new BestTreeAlgorithm.Result(cost, tree);
    }

    public static class Context {
        private final Graph weights;
        private final DisjointSet ds;
        private final WeighedEdge[] edges;
        private final int maximumDegree;

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

        private Context(Graph g, int maximumDegree) {
            int n = g.nVertices();
            this.weights = g;
            this.maximumDegree = maximumDegree;
            ds = new DisjointSet(n);
            edges = new WeighedEdge[g.nEdges()];
            for (int i = 0, ei = 0; i < n; ++i) {
                int nAdj = g.degree(i);
                for (int j = 0; j < nAdj; ++j) {
                    int t = g.getDestination(i, j);
                    if (i < t) {
                        edges[ei] = new WeighedEdge(i, t, g.getWeight(i, j));
                        ++ei;
                    }
                }
            }
            Arrays.sort(edges);
        }

        private void reset() {
            ds.reset();
        }
    }
}
