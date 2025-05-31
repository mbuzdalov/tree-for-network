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

    public static void main(String[] args) throws IOException {
        GraphWithDegree input = getGraph(args[0]);
        String output = args[1];
        long timeLimit = Long.parseLong(args[2]) * 1000; // to milliseconds
        int iterations = Integer.parseInt(args[3]);

        List<NamedBestTreeAlgorithm> treeAlgos = input.maxDegree == 3
                ? List.of(NamedBestTreeAlgorithm.byName("MST+random"),
                          NamedBestTreeAlgorithm.byName("MST+replaceOB"),
                          NamedBestTreeAlgorithm.byName("BST+replaceOB"))
                : List.of(NamedBestTreeAlgorithm.byName("MST+random"));

        RandomGenerator random = factory.create();
        long bestResult = Long.MAX_VALUE;
        long bestTree = Long.MAX_VALUE;

        Path fitnessStamp = Paths.get(output + ".fitness");
        if (Files.exists(fitnessStamp)) {
            bestResult = Long.parseLong(Files.readString(fitnessStamp));
        }

        for (int iteration = 0; iteration < iterations; ++iteration) {
            for (NamedBestTreeAlgorithm treeAlgo : treeAlgos) {
                System.out.println("Iteration " + iteration + ", algorithm " + treeAlgo.name());
                Timer t1 = Timer.newFixedTimer(System.currentTimeMillis(), timeLimit);
                BestTreeAlgorithm.Result treeResult = treeAlgo.algorithm().solve(input.graph, input.maxDegree,
                        t1, random, (a, b) -> {}).result();
                long cost0 = treeResult.cost();
                if (cost0 < bestTree) {
                    bestTree = cost0;
                }
                if (cost0 < bestResult) {
                    bestResult = cost0;
                    writeOutput(output, treeResult.tree(), bestResult);
                    System.out.println("  Update to " + bestResult + " with generated tree");
                } else {
                    System.out.println("  Generated tree has cost " + cost0 + " (best tree is " + bestTree + ")");
                }
                Timer t2 = Timer.newFixedTimer(System.currentTimeMillis(), timeLimit / 10);
                do {
                    BoundedSimpleGraph graph = insertPossibleEdges(input.maxDegree, treeResult.tree(), random);
                    long cost1 = cost(input.graph, graph);
                    if (cost1 < bestResult) {
                        bestResult = cost1;
                        writeOutput(output, graph, bestResult);
                        System.out.println("  Update to " + bestResult + " with graph from tree");
                    }
                } while (!t2.shouldInterrupt());
            }
        }
    }
}
