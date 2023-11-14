package com.github.mbuzdalov.tree4network;

import com.github.mbuzdalov.tree4network.algo.*;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.cost.DefaultCostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.io.GraphFromCSV;
import com.github.mbuzdalov.tree4network.mut.EdgeOptimalRelinkMutation;
import com.github.mbuzdalov.tree4network.mut.EdgeSwitchMutation;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    private static final List<BestTreeAlgorithm> algorithms = List.of(
            new BestMSTOverEdgeShuffle(),
            new BestBSTOverRandomPermutations(),
            new BestBSTOverAllPermutations(),
            new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeSwitchMutation.getInstance()),
            new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeSwitchMutation.getInstance()),
            new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeOptimalRelinkMutation.getInstance()),
            new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeOptimalRelinkMutation.getInstance())
    );

    public static void main(String[] args) throws IOException {
        File[] tests = new File("data").listFiles();
        if (tests == null) {
            System.err.println("Cannot retrieve the list of test files");
            System.exit(1);
        }
        long timeLimitMillis = 6000;

        System.out.println("Time limit: " + timeLimitMillis + " milliseconds");
        for (File test : tests) {
            System.out.println("Processing " + test.getName());
            Graph graph = GraphFromCSV.fromGZippedFile(test);
            int nEdges = 0;
            for (int i = 0; i < graph.nVertices(); ++i) {
                nEdges += graph.degree(i);
            }
            CostComputationAlgorithm cost = new DefaultCostComputationAlgorithm(graph.nVertices());
            System.out.println("  Graph has " + graph.nVertices() + " vertices and " + (nEdges / 2) + " edges");
            for (BestTreeAlgorithm algo : algorithms) {
                Timer timer = Timer.newFixedTimer(System.currentTimeMillis(), timeLimitMillis);
                BestTreeAlgorithm.Result result = algo.solve(graph, timer);
                if (result != null) {
                    System.out.println("  " + algo.getName() + ": " + result.cost() + " in " + timer.timeConsumedMillis() + " milliseconds");
                    if (cost.compute(graph, result.tree()) != result.cost()) {
                        throw new AssertionError("Independent cost computation failed");
                    }
                } else {
                    System.out.println("  " + algo.getName() + ": interrupted after " + timer.timeConsumedMillis() + " milliseconds");
                }
            }
        }
    }
}
