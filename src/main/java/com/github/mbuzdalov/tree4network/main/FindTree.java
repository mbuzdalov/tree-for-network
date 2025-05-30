package com.github.mbuzdalov.tree4network.main;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.*;
import com.github.mbuzdalov.tree4network.io.GraphFromCSV;
import com.github.mbuzdalov.tree4network.util.Timer;

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

public final class FindTree {
    // this is a main-only class
    private FindTree() {}

    // This is JDK 17 default
    private static final RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.of("L32X64MixRandom");

    private record NamedGraph(String name, Graph graph) {}

    private record Task(NamedGraph graph, NamedBestTreeAlgorithm algo, int maxDegree,
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
        List<NamedBestTreeAlgorithm> algorithms = NamedBestTreeAlgorithm.algorithms();
        for (int i = 0; i < algorithms.size(); ++i) {
            System.err.print(i == 0 ? "  " : ", ");
            System.err.print(algorithms.get(i).name());
        }
        System.err.println();
        System.exit(1);
    }

    private static NamedBestTreeAlgorithm getAlgorithm(String algoName) {
        Optional<NamedBestTreeAlgorithm> maybeAlgo = NamedBestTreeAlgorithm.algorithms()
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

    private static NamedGraph getGraph(String filename) {
        try {
            File file = new File(filename);
            Graph g = GraphFromCSV.fromGZippedFile(file);
            String name = file.getName();
            name = name.substring(0, name.indexOf(".csv.gz"));
            return new NamedGraph(name, g);
        } catch (Throwable th) {
            System.err.println("Error: cannot read graph '" + filename + "'");
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

        NamedBestTreeAlgorithm algo = getAlgorithm(args[0]);
        NamedGraph graph = getGraph(args[1]);
        int maxDegree = getMaxDegree(args[2]);
        String runID = args[3];
        long timeLimitMillis = getTimeout(args[4]) * 1000;
        try (PrintWriter log = new PrintWriter(new FileWriter(args[5], true))) {
            new Task(graph, algo, maxDegree, runID, timeLimitMillis, log).run();
        }
    }
}
