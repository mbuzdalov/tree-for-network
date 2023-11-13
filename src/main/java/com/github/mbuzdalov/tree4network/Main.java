package com.github.mbuzdalov.tree4network;

import com.github.mbuzdalov.tree4network.algo.BestBSTOverAllPermutations;
import com.github.mbuzdalov.tree4network.algo.BestBSTOverRandomPermutations;
import com.github.mbuzdalov.tree4network.algo.BestMSTOverEdgeShuffle;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.cost.DefaultCostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.io.GraphFromCSV;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    private static final List<BestTreeAlgorithm> algorithms = List.of(
            new BestMSTOverEdgeShuffle(),
            new BestBSTOverRandomPermutations(),
            new BestBSTOverAllPermutations()
    );

    public static void main(String[] args) throws IOException {
        File[] tests = new File("data").listFiles();
        if (tests == null) {
            System.err.println("Cannot retrieve the list of test files");
            System.exit(1);
        }
        long timeLimitMillis = 60000;

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
                long t0 = System.currentTimeMillis();
                BestTreeAlgorithm.Result result = algo.construct(graph, timeLimitMillis);
                if (result != null) {
                    System.out.println("  " + algo.getName() + ": " + result.cost() + " in " + (System.currentTimeMillis() - t0) + " milliseconds");
                    if (cost.compute(graph, result.tree()) != result.cost()) {
                        throw new AssertionError("Independent cost computation failed");
                    }
                } else {
                    System.out.println("  " + algo.getName() + ": interrupted after " + (System.currentTimeMillis() - t0) + " milliseconds");
                }
            }
        }
    }
}
