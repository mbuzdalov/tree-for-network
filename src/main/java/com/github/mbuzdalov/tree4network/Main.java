package com.github.mbuzdalov.tree4network;

import com.github.mbuzdalov.tree4network.algo.*;
import com.github.mbuzdalov.tree4network.io.GraphFromCSV;
import com.github.mbuzdalov.tree4network.mut.EdgeOptimalRelinkMutation;
import com.github.mbuzdalov.tree4network.mut.EdgeRandomRelinkMutation;
import com.github.mbuzdalov.tree4network.mut.EdgeSwitchMutation;
import com.github.mbuzdalov.tree4network.mut.SubtreeSwapMutation;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final List<BestTreeAlgorithm> algorithms = List.of(
            new BestMSTOverEdgeShuffle(),
            new BestBSTOverRandomPermutations(),
            new BestBSTOverAllPermutations(),
            new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeSwitchMutation.getInstance()),
            new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), SubtreeSwapMutation.getInstance()),
            new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeRandomRelinkMutation.getInstance()),
            new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeOptimalRelinkMutation.getInstance()),
            new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeSwitchMutation.getInstance()),
            new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), SubtreeSwapMutation.getInstance()),
            new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeRandomRelinkMutation.getInstance()),
            new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeOptimalRelinkMutation.getInstance())
    );

    private record Task(String graphName, Graph graph, int algoIndex, int runIndex, long timeLimitMillis, PrintWriter log) implements Runnable {
        private void writeFitness(long time, long fitness) {
            synchronized (log) {
                log.println(graphName + "," + algoIndex + "," + runIndex + "," + time + "," + fitness);
            }
        }

        @Override
        public void run() {
            Timer timer = Timer.newFixedTimer(System.currentTimeMillis(), timeLimitMillis);
            BestTreeAlgorithm algo = algorithms.get(algoIndex);
            BestTreeAlgorithm.ExtendedResult result = algo.solve(graph, timer, this::writeFitness);
            synchronized (System.out) {
                System.out.print(graphName + "," + algoIndex + "," + runIndex + "," + timer.timeConsumedMillis() + ": ");
                if (result.result() != null) {
                    System.out.println(result.result().cost() + ", " + result.nQueries() + " queries");
                } else {
                    System.out.println("interrupted");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File[] tests = new File("data").listFiles();
        if (tests == null) {
            System.err.println("Cannot retrieve the list of test files");
            System.exit(1);
        }
        long timeLimitMillis = 1000L * Integer.parseInt(args[0]);
        int nCPUs = Integer.parseInt(args[1]);
        int nRuns = Integer.parseInt(args[2]);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(nCPUs);
        try (PrintWriter log = new PrintWriter(new FileWriter("log.csv"), true)) {
            for (File test : tests) {
                Graph graph = GraphFromCSV.fromGZippedFile(test);
                for (int runIndex = 0; runIndex < nRuns; ++runIndex) {
                    for (int algoIndex = 0; algoIndex < algorithms.size(); ++algoIndex) {
                        executor.execute(new Task(test.getName(), graph, algoIndex, runIndex, timeLimitMillis, log));
                    }
                }
            }

            executor.shutdown();
            while (!executor.awaitTermination(10, TimeUnit.DAYS)) {
                System.out.println("Executor is still not terminated");
            }
        }
    }
}
