package com.github.mbuzdalov.tree4network.xover;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.DisjointSet;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class PartitionCrossover implements Crossover<PartitionCrossover.Context> {
    private static final PartitionCrossover INSTANCE = new PartitionCrossover();
    private PartitionCrossover() {}

    public static PartitionCrossover getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "partition crossover";
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
        int n = weights.nVertices();
        BoundedForest graphA = resultA.tree();
        BoundedForest graphB = resultB.tree();
        BoundedForest common = new BoundedForest(n);
        GraphBuilder xorBuilder = new GraphBuilder();
        for (int v = 0; v < n; ++v) {
            int dA = graphA.degree(v);
            for (int e = 0; e < dA; ++e) {
                int w = graphA.getDestination(v, e);
                if (v < w) {
                    if (graphB.hasEdge(v, w)) {
                        common.addEdge(v, w);
                    } else {
                        xorBuilder.addEdge(v, w, 1);
                    }
                }
            }
            int dB = graphB.degree(v);
            for (int e = 0; e < dB; ++e) {
                int w = graphB.getDestination(v, e);
                if (v < w) {
                    if (!graphA.hasEdge(v, w)) {
                        xorBuilder.addEdge(v, w, 2);
                    }
                }
            }
        }
        Graph xor = xorBuilder.result();
        context.reset();
        int nComponents = context.nComponents(xor);
        context.setInitResults(resultA, resultB, weights, costAlgo);
        if (nComponents > 1) {
            System.out.print("Common edges: " + common.nEdges() + ", xor edges: " + xor.nEdges() + ", xor components: " + nComponents + ":");
            context.go(xor, common, 1, nComponents, timer);
            System.out.println(" Done! [best crossover cost = " + context.bestCrossoverCost + "]");
        }
        return context.bestResult;
    }

    public static class Context {
        private final int[] component;
        private BestTreeAlgorithm.Result bestResult;
        private long bestCrossoverCost = Long.MAX_VALUE;
        private CostComputationAlgorithm costAlgo;
        private Graph weights;

        private Context(int n) {
            component = new int[n];
        }

        private void reset() {
        }

        private void fill(Graph g, int v, int comp) {
            int d = g.degree(v);
            for (int e = 0; e < d; ++e) {
                int w = g.getDestination(v, e);
                if (component[w] == 0) {
                    component[w] = comp;
                    fill(g, w, comp);
                }
            }
        }

        private int nComponents(Graph g) {
            int nc = 0;
            Arrays.fill(component, 0);
            for (int i = 0; i < component.length; ++i) {
                if (component[i] == 0) {
                    if (i < g.nVertices() && g.degree(i) > 0) {
                        ++nc;
                        component[i] = nc;
                        fill(g, i, nc);
                    }
                }
            }
            return nc;
        }

        private void setInitResults(BestTreeAlgorithm.Result a, BestTreeAlgorithm.Result b,
                                    Graph weights, CostComputationAlgorithm costAlgo) {
            if (a.cost() < b.cost()) {
                bestResult = a;
            } else {
                bestResult = b;
            }
            this.weights = weights;
            this.costAlgo = costAlgo;
        }

        private void go(Graph xor, BoundedForest current, int nComp, int maxComp, Timer timer) {
            if (timer.shouldInterrupt()) {
                return;
            }
            if (nComp > maxComp) {
                if (current.nEdges() + 1 == current.nVertices()) {
                    long cost = costAlgo.compute(weights, current);
                    if (cost < bestResult.cost()) {
                        bestCrossoverCost = cost;
                        System.out.print(" [" + bestResult.cost() + " => " + cost + "]");
                        bestResult = new BestTreeAlgorithm.Result(cost, new BoundedForest(current));
                    }
                }
            } else {
                int n = current.nVertices();
                for (int vComp = 1; vComp <= 2; ++vComp) {
                    DisjointSet ds = new DisjointSet(n);
                    for (int v = 0; v < n; ++v) {
                        int d = current.degree(v);
                        for (int e = 0; e < d; ++e) {
                            ds.unite(v, current.getDestination(v, e));
                        }
                    }
                    BoundedForest next = new BoundedForest(current);
                    boolean componentOK = true;
                    componentAssemblyLoop:
                    for (int v = 0; v < n; ++v) {
                        if (component[v] == nComp) {
                            int d = xor.degree(v);
                            for (int e = 0; e < d; ++e) {
                                int w = xor.getDestination(v, e);
                                if (component[w] != nComp) throw new AssertionError();
                                if (v < w && xor.getWeight(v, e) == vComp) {
                                    if (ds.get(v) == ds.get(w) || next.degree(v) == 3 || next.degree(w) == 3) {
                                        // fail
                                        componentOK = false;
                                        break componentAssemblyLoop;
                                    }
                                    ds.unite(v, w);
                                    next.addEdge(v, w);
                                }
                            }
                        }
                    }
                    if (componentOK) {
                        go(xor, next, nComp + 1, maxComp, timer);
                    }
                }
            }
        }
    }
}
