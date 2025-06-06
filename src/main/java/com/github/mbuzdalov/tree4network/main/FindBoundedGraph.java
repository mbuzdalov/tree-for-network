package com.github.mbuzdalov.tree4network.main;

import com.github.mbuzdalov.tree4network.BoundedSimpleGraph;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class FindBoundedGraph {
    // this is a main-only class
    private FindBoundedGraph() {}

    // This is JDK 17 default
    private static final RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.of("L32X64MixRandom");

    private static BoundedSimpleGraph insertPossibleEdges(int maxDegree,
                                                          BoundedSimpleGraph tree, RandomGenerator random) {
        BoundedSimpleGraph result = tree.copyWithNewMaximumDegree(maxDegree);
        int[] smallDegrees = new int[result.nVertices()];
        int nSmall = 0;
        for (int i = 0; i < result.nVertices(); ++i) {
            if (result.degree(i) < maxDegree) {
                smallDegrees[nSmall] = i;
                ++nSmall;
            }
        }
        while (nSmall >= 2) {
            int i1 = random.nextInt(nSmall);
            int i2 = random.nextInt(nSmall - 1);
            // the logic below ensures i1 < i2 and uniform sampling from all pairs
            if (i2 >= i1) {
                ++i2;
            } else {
                int tmp = i1;
                i1 = i2;
                i2 = tmp;
            }
            int v1 = smallDegrees[i1];
            int v2 = smallDegrees[i2];
            result.addEdge(v1, v2);
            if (result.degree(v2) == maxDegree) {
                smallDegrees[i2] = smallDegrees[--nSmall];
            }
            if (result.degree(v1) == maxDegree) {
                smallDegrees[i1] = smallDegrees[--nSmall];
            }
        }
        return result;
    }

    private static long tryImprove(BoundedSimpleGraph graph, Graph weights,
                                   long oldCost, RandomGenerator random) {
        int n = graph.nVertices();

        while (true) {
            int v1 = random.nextInt(n);
            int v1d = graph.degree(v1);
            if (v1d == 0) {
                continue;
            }
            int v2 = graph.getDestination(v1, random.nextInt(v1d));
            int v3;
            do {
                v3 = random.nextInt(n);
            } while (v3 == v1 || v3 == v2 || graph.degree(v3) == 0);
            int v3d = graph.degree(v3);
            int countNotEqual = 0;
            for (int i = 0; i < v3d; ++i) {
                int v4 = graph.getDestination(v3, i);
                if (v4 != v1 && v4 != v2) {
                    ++countNotEqual;
                }
            }
            if (countNotEqual == 0) {
                continue;
            }
            int v4;
            do {
                v4 = graph.getDestination(v3, random.nextInt(v3d));
            } while (v1 == v4 || v2 == v4);

            graph.removeEdge(v1, v2);
            graph.removeEdge(v3, v4);

            // attempt 1
            graph.addEdge(v1, v4);
            graph.addEdge(v2, v3);
            long newCost = cost(weights, graph);
            if (newCost < oldCost) {
                return newCost;
            }

            // attempt 2
            graph.removeEdge(v1, v4);
            graph.removeEdge(v2, v3);
            graph.addEdge(v1, v3);
            graph.addEdge(v2, v4);
            long newCost2 = cost(weights, graph);
            if (newCost2 < oldCost) {
                return newCost2;
            }

            // roll back
            graph.removeEdge(v1, v3);
            graph.removeEdge(v2, v4);
            graph.addEdge(v1, v2);
            graph.addEdge(v3, v4);
            return oldCost;
        }
    }

    private static long cost(Graph weights, BoundedSimpleGraph graph) {
        long result = 0;
        int n = weights.nVertices();
        int[] queue = new int[n];
        int[] dist = new int[n];
        for (int i = 0; i < n; ++i) {
            int head = 0, tail = 0;
            queue[head++] = i;
            Arrays.fill(dist, Integer.MAX_VALUE);
            dist[i] = 0;
            while (head > tail) {
                int curr = queue[tail++];
                int nextDist = dist[curr] + 1;
                int currDegree = graph.degree(curr);
                for (int j = 0; j < currDegree; ++j) {
                    int next = graph.getDestination(curr, j);
                    if (dist[next] > nextDist) {
                        dist[next] = nextDist;
                        queue[head++] = next;
                    }
                }
            }
            int wDeg = weights.degree(i);
            for (int j = 0; j < wDeg; ++j) {
                int next = weights.getDestination(i, j);
                int weight = weights.getWeight(i, j);
                if (dist[next] == Integer.MAX_VALUE) {
                    return Long.MAX_VALUE;
                }
                result += (long) (weight) * dist[next];
            }
        }
        return result / 2;
    }

    private record GraphWithDegree(Graph graph, int maxDegree) {}

    private static GraphWithDegree getGraph(String filename) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            StringTokenizer header = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(header.nextToken());
            int m = Integer.parseInt(header.nextToken());
            int d = Integer.parseInt(header.nextToken());
            GraphBuilder b = new GraphBuilder();
            b.setNumberOfVertices(n);
            for (int i = 0; i < m; ++i) {
                StringTokenizer line = new StringTokenizer(in.readLine());
                int src = Integer.parseInt(line.nextToken()) - 1;
                int dst = Integer.parseInt(line.nextToken()) - 1;
                int weight = Integer.parseInt(line.nextToken());
                b.addEdge(src, dst, weight);
            }
            return new GraphWithDegree(b.result(), d);
        } catch (Throwable th) {
            System.err.println("Error: cannot read graph '" + filename + "'");
            th.printStackTrace(System.err);
            System.exit(1);
            throw new IllegalStateException("System.exit(1) failed");
        }
    }

    private static void writeOutput(String filename, BoundedSimpleGraph graph, long cost) throws IOException {
        try (PrintWriter out = new PrintWriter(filename)) {
            int nEdges = 0;
            for (int i = 0; i < graph.nVertices(); ++i) {
                nEdges += graph.degree(i);
            }
            out.println(nEdges / 2);
            for (int i = 0; i < graph.nVertices(); ++i) {
                int deg = graph.degree(i);
                for (int j = 0; j < deg; ++j) {
                    int next = graph.getDestination(i, j);
                    if (next > i) {
                        out.println((i + 1) + " " + (next + 1));
                    }
                }
            }
        }
        try (PrintWriter out = new PrintWriter(filename + ".fitness")) {
            out.println(cost);
        }
    }

    private static class Partitioner {
        private final int[] components;
        private final int[][] bwdIndexers;

        private final Graph[] subgraphs;

        Partitioner(Graph graph) {
            int n = graph.nVertices();
            components = new int[n];
            int[] queue = new int[n];
            int nComponents = 0;
            for (int i = 0; i < n; ++i) {
                if (components[i] == 0) {
                    ++nComponents;
                    components[i] = nComponents;
                    int head = 0, tail = 0;
                    queue[head++] = i;
                    while (head > tail) {
                        int curr = queue[tail++];
                        int d = graph.degree(curr);
                        for (int j = 0; j < d; ++j) {
                            int next = graph.getDestination(curr, j);
                            if (components[next] == 0) {
                                components[next] = nComponents;
                                queue[head++] = next;
                            }
                        }
                    }
                }
            }

            if (nComponents == 1) {
                this.subgraphs = new Graph[] { graph };
                this.bwdIndexers = null;
            } else {
                this.bwdIndexers = new int[nComponents][];

                int[] fwdIndexer = new int[n];
                Arrays.fill(fwdIndexer, -1);

                int[] sizes = new int[nComponents];
                GraphBuilder[] builders = new GraphBuilder[nComponents];
                for (int i = 0; i < n; ++i) {
                    ++sizes[components[i] - 1];
                }
                int nNonTrivialComponents = 0;
                for (int i = 0; i < nComponents; ++i) {
                    bwdIndexers[i] = new int[sizes[i]];
                    builders[i] = new GraphBuilder();
                    if (sizes[i] >= 4) {
                        ++nNonTrivialComponents;
                    }
                    sizes[i] = 0;
                }
                for (int i = 0; i < n; ++i) {
                    int comp = components[i] - 1;
                    int vIndex = sizes[comp]++;
                    fwdIndexer[i] = vIndex;
                    bwdIndexers[comp][vIndex] = i;
                }
                for (int curr = 0; curr < n; ++curr) {
                    int d = graph.degree(curr);
                    int comp = components[curr] - 1;
                    int newCurr = fwdIndexer[curr];
                    for (int i = 0; i < d; ++i) {
                        int next = graph.getDestination(curr, i);
                        int w = graph.getWeight(curr, i);
                        if (components[next] - 1 != comp) {
                            throw new AssertionError();
                        }
                        int newNext = fwdIndexer[next];
                        if (newCurr < newNext) {
                            builders[comp].addEdge(newCurr, newNext, w);
                        }
                    }
                }
                this.subgraphs = new Graph[nNonTrivialComponents];
                for (int i = 0, j = 0; i < nComponents; ++i) {
                    if (sizes[i] >= 4) {
                        subgraphs[j++] = builders[i].result();
                    }
                }
                System.out.println(nComponents + " subgraphs, of which " + nNonTrivialComponents + " non-trivial");
            }
        }

        Graph[] getSubgraphs() {
            return subgraphs;
        }

        BoundedSimpleGraph recombineResults(BoundedSimpleGraph[] graphs) {
            if (graphs.length == 1) {
                return graphs[0];
            } else {
                BoundedSimpleGraph rv = new BoundedSimpleGraph(components.length, graphs[0].maximumDegree());
                for (int comp0 = 0, comp = 0; comp0 < bwdIndexers.length; ++comp0) {
                    int[] bw = bwdIndexers[comp0];
                    int compSize = bw.length;
                    if (compSize <= 3) {
                        for (int a = 0; a < compSize; ++a) {
                            for (int b = a + 1; b < compSize; ++b) {
                                rv.addEdge(bw[a], bw[b]);
                            }
                        }
                    } else {
                        BoundedSimpleGraph component = graphs[comp];
                        ++comp;
                        for (int curr = 0; curr < component.nVertices(); ++curr) {
                            int currBack = bw[curr];
                            int d = component.degree(curr);
                            for (int i = 0; i < d; ++i) {
                                int next = component.getDestination(curr, i);
                                int nextBack = bw[next];
                                if (currBack < nextBack) {
                                    rv.addEdge(currBack, nextBack);
                                }
                            }
                        }
                    }
                }
                return rv;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GraphWithDegree input = getGraph(args[0]);
        String output = args[1];
        long timeLimit = Long.parseLong(args[2]) * 1000; // to milliseconds
        int iterations = Integer.parseInt(args[3]);

        List<NamedBestTreeAlgorithm> treeAlgos = input.maxDegree == 3 && input.graph.nVertices() <= 1024
                ? List.of(NamedBestTreeAlgorithm.byName("MST+random"),
                          NamedBestTreeAlgorithm.byName("MST+replaceOB"),
                          NamedBestTreeAlgorithm.byName("BST+replaceOB"))
                : List.of(NamedBestTreeAlgorithm.byName("MST+random"));

        RandomGenerator random = factory.create();
        long bestResult = Long.MAX_VALUE;

        Path fitnessStamp = Paths.get(output + ".fitness");
        if (Files.exists(fitnessStamp)) {
            bestResult = Long.parseLong(Files.readString(fitnessStamp).trim());
        }

        Partitioner p = new Partitioner(input.graph);
        Graph[] components = p.getSubgraphs();
        BoundedSimpleGraph[] results = new BoundedSimpleGraph[components.length];

        long sumBestComponents = 0;

        for (int subgraph = 0; subgraph < components.length; ++subgraph) {
            System.out.println("Starting subgraph " + (subgraph + 1) + " of " + components.length);
            Graph g = components[subgraph];
            int gSize = g.nVertices();
            int bigSize = input.graph.nVertices();
            long projectedTimeLimit = Math.max(1000, (long) Math.ceil((double) (timeLimit) * gSize * gSize / bigSize / bigSize));
            System.out.println("  Component size is " + gSize + " of " + bigSize + ", using time limit " + projectedTimeLimit);

            long componentBestResult = Long.MAX_VALUE;

            // Phase 1. Go find the best tree
            System.out.println("  Phase 1: Constructing good tree using " + iterations + " iterations and " + treeAlgos.size() + " tree algorithms");
            BestTreeAlgorithm.Result bestTree = null;
            for (int iteration = 1; iteration <= iterations; ++iteration) {
                for (NamedBestTreeAlgorithm treeAlgo : treeAlgos) {
                    System.out.print("    Iteration " + iteration + ", algorithm " + treeAlgo.name() + ": ");
                    long localTimeLimit = Math.max(1000, projectedTimeLimit / iterations / treeAlgos.size());
                    Timer t1 = Timer.newFixedTimer(System.currentTimeMillis(), localTimeLimit);
                    BestTreeAlgorithm.Result treeResult = treeAlgo.algorithm().solve(g, input.maxDegree,
                            t1, random, (a, b) -> {}).result();
                    if (treeResult != null) {
                        long cost0 = treeResult.cost();
                        if (bestTree == null || cost0 < bestTree.cost()) {
                            bestTree = treeResult;
                        }
                        if (cost0 < componentBestResult) {
                            componentBestResult = cost0;
                            results[subgraph] = treeResult.tree();
                            System.out.println("update to " + componentBestResult);
                        } else {
                            System.out.println("just " + cost0);
                        }
                    } else {
                        System.out.println("timed out");
                    }
                }
            }

            if (bestTree == null) {
                System.out.println("ERROR: Could not find any tree. Exiting...");
                return;
            }

            // Phase 2. Attempt connecting
            BoundedSimpleGraph bestConnected = bestTree.tree();
            System.out.println("  Phase 2: Find good connections within 1/2 of the tree time limit");
            Timer t2 = Timer.newFixedTimer(System.currentTimeMillis(), projectedTimeLimit / 2);
            while (!t2.shouldInterrupt()) {
                BoundedSimpleGraph graph = insertPossibleEdges(input.maxDegree, bestTree.tree(), random);
                long cost1 = cost(g, graph);
                if (cost1 < componentBestResult) {
                    componentBestResult = cost1;
                    results[subgraph] = graph;
                    bestConnected = graph;
                    System.out.println("    Update to " + componentBestResult);
                }

            }

            // Phase 3. Gradient descent
            System.out.println("  Phase 3: Running RLS within 1/2 of the tree time limit");
            Timer t3 = Timer.newFixedTimer(System.currentTimeMillis(), projectedTimeLimit / 2);
            while (!t3.shouldInterrupt()) {
                long cost2 = tryImprove(bestConnected, g, componentBestResult, random);
                if (cost2 < componentBestResult) {
                    componentBestResult = cost2;
                    System.out.println("    Update to " + componentBestResult);
                }
            }

            sumBestComponents += componentBestResult;
        }

        System.out.println("Sum of best component-wise results is " + sumBestComponents + ", but this does not include trivial subgraphs");
        BoundedSimpleGraph finalResult = p.recombineResults(results);
        long finalCost = cost(input.graph, finalResult);
        System.out.println("Final result has cost " + finalCost);
        if (finalCost < bestResult) {
            if (bestResult != Long.MAX_VALUE) {
                System.out.println("  Overwriting the output file because " + finalCost + " < " + bestResult);
            }
            writeOutput(output, finalResult, finalCost);
        }
    }
}
