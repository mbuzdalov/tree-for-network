package com.github.mbuzdalov.tree4network;

import com.github.mbuzdalov.tree4network.algo.*;
import com.github.mbuzdalov.tree4network.io.GraphFromCSV;
import com.github.mbuzdalov.tree4network.mut.*;
import com.github.mbuzdalov.tree4network.util.Timer;
import com.github.mbuzdalov.tree4network.xover.PartitionCrossover;
import com.github.mbuzdalov.tree4network.xover.RandomEdgeSubsetCrossover;
import com.github.mbuzdalov.tree4network.xover.GreedyEdgeSubsetCrossover;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class Main {
    // This is JDK 17 default
    private static final RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.of("L32X64MixRandom");

    private record NamedAlgorithm(String name, BestTreeAlgorithm algorithm) {}
    private record NamedGraph(String name, Graph graph) {}

    private static final List<NamedAlgorithm> algorithms = List.of(
            new NamedAlgorithm("MST", new BestMSTOverEdgeShuffle()),
            new NamedAlgorithm("BST/rand", new BestBSTOverRandomPermutations()),
            new NamedAlgorithm("BST/next", new BestBSTOverAllPermutations()),
            new NamedAlgorithm("MST+switch", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeSwitchMutation.getInstance())),
            new NamedAlgorithm("MST+subtree", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), SubtreeSwapMutation.getInstance())),
            new NamedAlgorithm("MST+replaceR", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeRandomRelinkMutation.getInstance())),
            new NamedAlgorithm("MST+replaceO", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeOptimalRelinkMutation.getInstance())),
            new NamedAlgorithm("MST+replaceOB", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), OptimalMoveMutation.getInstance())),
            new NamedAlgorithm("MST+bstMut", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), RandomBSTTraversalMutation.getInstance())),
            new NamedAlgorithm("MST+random", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), RandomChoiceMutation.getInstance())),
            new NamedAlgorithm("BST+switch", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeSwitchMutation.getInstance())),
            new NamedAlgorithm("BST+subtree", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), SubtreeSwapMutation.getInstance())),
            new NamedAlgorithm("BST+replaceR", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeRandomRelinkMutation.getInstance())),
            new NamedAlgorithm("BST+replaceO", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeOptimalRelinkMutation.getInstance())),
            new NamedAlgorithm("BST+replaceOB", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), OptimalMoveMutation.getInstance())),
            new NamedAlgorithm("BST+random", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), RandomChoiceMutation.getInstance())),
            new NamedAlgorithm("BST+bstMut", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), RandomBSTTraversalMutation.getInstance())),

            new NamedAlgorithm("MST+switch+randomES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeSwitchMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),
            new NamedAlgorithm("MST+replaceO+randomES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeOptimalRelinkMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),
            new NamedAlgorithm("BST+switch+randomES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeSwitchMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),
            new NamedAlgorithm("BST+replaceO+randomES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeOptimalRelinkMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),

            new NamedAlgorithm("MST+switch+greedyES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeSwitchMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),
            new NamedAlgorithm("MST+replaceO+greedyES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeOptimalRelinkMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),
            new NamedAlgorithm("BST+switch+greedyES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeSwitchMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),
            new NamedAlgorithm("BST+replaceO+greedyES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeOptimalRelinkMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),

            new NamedAlgorithm("MST+switch+PX", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeSwitchMutation.getInstance(), PartitionCrossover.getInstance())),
            new NamedAlgorithm("MST+replaceO+PX", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeOptimalRelinkMutation.getInstance(), PartitionCrossover.getInstance())),
            new NamedAlgorithm("BST+switch+PX", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeSwitchMutation.getInstance(), PartitionCrossover.getInstance())),
            new NamedAlgorithm("BST+replaceO+PX", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeOptimalRelinkMutation.getInstance(), PartitionCrossover.getInstance()))

    );

    private record Task(NamedGraph graph, NamedAlgorithm algo, int maxDegree,
                        String runID, long timeLimitMillis, PrintWriter log) implements Runnable {
        private void writeFitness(long time, long fitness) {
            log.println(graph.name() + "," + algo.name() + "," + maxDegree + "," + runID + "," + time + "," + fitness);
        }

        @Override
        public void run() {
            byte[] seed = (graph.name() + "::" + algo.name() + "::" + maxDegree + "::" + runID).getBytes();

            // The factory.create(seed) constructor does not do what I want:
            // it is unfortunately not guaranteed that all bytes in seed will be used.
            // So we need to re-hash these.

            try {
                seed = MessageDigest.getInstance("SHA-256").digest(seed);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Java is guaranteed to have SHA-256");
            }

            RandomGenerator random = factory.create(seed);

            Timer timer = Timer.newFixedTimer(System.currentTimeMillis(), timeLimitMillis);
            BestTreeAlgorithm.ExtendedResult result = algo.algorithm().solve(graph.graph(), maxDegree, timer, random, this::writeFitness);
            System.out.print(graph.name() + "," + algo.name() + "," + maxDegree + "," + runID + "," + timer.timeConsumedMillis() + ": ");
            if (result.result() != null) {
                System.out.println(result.result().cost() + ", " + result.nQueries() + " queries");
            } else {
                System.out.println("interrupted");
            }
        }
    }

    private static void usage() {
        System.err.println("Usage: <algo> <file> <runID> <timeout> <fitness-log>, where:");
        System.err.println("  <algo>         the name of the algorithm to run");
        System.err.println("  <file>         the dataset to run the algorithm on");
        System.err.println("  <maxDegree>    the maximum degree of a vertex in the answer (should be at least 2)");
        System.err.println("  <runID>        a string that gets logged verbatim to indicate the run");
        System.err.println("  <timeout>      the maximum runtime, in seconds");
        System.err.println("  <fitness-log>  the file for fitness trajectory logging");
        System.err.println("Available algorithms are: ");
        for (int i = 0; i < algorithms.size(); ++i) {
            System.err.print(i == 0 ? "  " : ", ");
            System.err.print(algorithms.get(i).name());
        }
        System.err.println();
        System.exit(1);
    }

    private static NamedAlgorithm getAlgorithm(String[] args) {
        String algoName = args[0];
        Optional<NamedAlgorithm> maybeAlgo = algorithms
                .stream()
                .filter(a -> a.name().equals(algoName))
                .findFirst();
        if (maybeAlgo.isEmpty()) {
            System.err.println("Error: unknown algorithm '" + algoName + "'");
            usage();
            throw new IllegalStateException("System.exit(1) failed");
        }
        return maybeAlgo.get();
    }

    private static NamedGraph getGraph(String[] args) {
        String fileName = args[1];
        try {
            File file = new File(fileName);
            Graph g = GraphFromCSV.fromGZippedFile(file);
            String name = file.getName();
            name = name.substring(0, name.indexOf(".csv.gz"));
            return new NamedGraph(name, g);
        } catch (Throwable th) {
            System.err.println("Error: cannot read graph '" + fileName + "'");
            th.printStackTrace(System.err);
            usage();
            throw new IllegalStateException("System.exit(1) failed");
        }
    }

    private static long getTimeout(String timeout) {
        try {
            long result = Long.parseLong(timeout);
            if (result <= 0) {
                System.err.println("Error: expected positive timeout, found '" + result + "'");
                usage();
                throw new IllegalStateException("System.exit(1) failed");
            }
            return result;
        } catch (NumberFormatException ex) {
            System.err.println("Error: expected number as timeout, found '" + timeout + "'");
            usage();
            throw new IllegalStateException("System.exit(1) failed");
        }
    }

    private static int getMaxDegree(String maxDegree) {
        try {
            int result = Integer.parseInt(maxDegree);
            if (result <= 1) {
                System.err.println("Error: expected maximum degree at least 2, found '" + result + "'");
                usage();
                throw new IllegalStateException("System.exit(1) failed");
            }
            return result;
        } catch (NumberFormatException ex) {
            System.err.println("Error: expected number as maximum degree, found '" + maxDegree + "'");
            usage();
            throw new IllegalStateException("System.exit(1) failed");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 6) {
            usage();
            return;
        }

        NamedAlgorithm algo = getAlgorithm(args);
        NamedGraph graph = getGraph(args);
        int maxDegree = getMaxDegree(args[2]);
        String runID = args[3];
        long timeLimitMillis = getTimeout(args[4]) * 1000;
        try (PrintWriter log = new PrintWriter(new FileWriter(args[5], true))) {
            new Task(graph, algo, maxDegree, runID, timeLimitMillis, log).run();
        }
    }
}
