package com.github.mbuzdalov.tree4network.main;

import com.github.mbuzdalov.tree4network.algo.*;
import com.github.mbuzdalov.tree4network.mut.*;
import com.github.mbuzdalov.tree4network.xover.GreedyEdgeSubsetCrossover;
import com.github.mbuzdalov.tree4network.xover.PartitionCrossover;
import com.github.mbuzdalov.tree4network.xover.RandomEdgeSubsetCrossover;

import java.util.List;

public record NamedBestTreeAlgorithm(String name, BestTreeAlgorithm algorithm) {
    private static final List<NamedBestTreeAlgorithm> algorithms = List.of(
            new NamedBestTreeAlgorithm("MST", new BestMSTOverEdgeShuffle()),
            new NamedBestTreeAlgorithm("BST/rand", new BestBSTOverRandomPermutations()),
            new NamedBestTreeAlgorithm("BST/next", new BestBSTOverAllPermutations()),
            new NamedBestTreeAlgorithm("MST+switch", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeSwitchMutation.getInstance())),
            new NamedBestTreeAlgorithm("MST+subtree", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), SubtreeSwapMutation.getInstance())),
            new NamedBestTreeAlgorithm("MST+replaceR", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeRandomRelinkMutation.getInstance())),
            new NamedBestTreeAlgorithm("MST+replaceO", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), EdgeOptimalRelinkMutation.getInstance())),
            new NamedBestTreeAlgorithm("MST+replaceOB", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), OptimalMoveMutation.getInstance())),
            new NamedBestTreeAlgorithm("MST+bstMut", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), RandomBSTTraversalMutation.getInstance())),
            new NamedBestTreeAlgorithm("MST+random", new SimpleLocalSearch<>(new BestMSTOverEdgeShuffle(), RandomChoiceMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+switch", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeSwitchMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+subtree", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), SubtreeSwapMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+replaceR", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeRandomRelinkMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+replaceO", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), EdgeOptimalRelinkMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+replaceOB", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), OptimalMoveMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+random", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), RandomChoiceMutation.getInstance())),
            new NamedBestTreeAlgorithm("BST+bstMut", new SimpleLocalSearch<>(new BestBSTOverRandomPermutations(), RandomBSTTraversalMutation.getInstance())),

            new NamedBestTreeAlgorithm("MST+switch+randomES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeSwitchMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),
            new NamedBestTreeAlgorithm("MST+replaceO+randomES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeOptimalRelinkMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),
            new NamedBestTreeAlgorithm("BST+switch+randomES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeSwitchMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),
            new NamedBestTreeAlgorithm("BST+replaceO+randomES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeOptimalRelinkMutation.getInstance(), RandomEdgeSubsetCrossover.getInstance())),

            new NamedBestTreeAlgorithm("MST+switch+greedyES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeSwitchMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),
            new NamedBestTreeAlgorithm("MST+replaceO+greedyES", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeOptimalRelinkMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),
            new NamedBestTreeAlgorithm("BST+switch+greedyES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeSwitchMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),
            new NamedBestTreeAlgorithm("BST+replaceO+greedyES", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeOptimalRelinkMutation.getInstance(), GreedyEdgeSubsetCrossover.getInstance())),

            new NamedBestTreeAlgorithm("MST+switch+PX", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeSwitchMutation.getInstance(), PartitionCrossover.getInstance())),
            new NamedBestTreeAlgorithm("MST+replaceO+PX", new LocalOptimaRecombiner<>(new BestMSTOverEdgeShuffle(),
                    EdgeOptimalRelinkMutation.getInstance(), PartitionCrossover.getInstance())),
            new NamedBestTreeAlgorithm("BST+switch+PX", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeSwitchMutation.getInstance(), PartitionCrossover.getInstance())),
            new NamedBestTreeAlgorithm("BST+replaceO+PX", new LocalOptimaRecombiner<>(new BestBSTOverRandomPermutations(),
                    EdgeOptimalRelinkMutation.getInstance(), PartitionCrossover.getInstance()))
    );

    public static List<NamedBestTreeAlgorithm> algorithms() {
        return algorithms;
    }
}
