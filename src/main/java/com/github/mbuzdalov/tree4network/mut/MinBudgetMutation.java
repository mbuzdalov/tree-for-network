package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class MinBudgetMutation implements Mutation<MinBudgetMutation.Context> {
    private static final MinBudgetMutation INSTANCE = new MinBudgetMutation();
    private MinBudgetMutation() {}

    public static MinBudgetMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Budgeted choice of mutation";
    }

    @Override
    public Context createContext(Graph weights) {
        return new Context(weights);
    }

    @Override
    public void resetContext(Context context) {
        context.reset();
    }

    @Override
    public BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights, Context context,
                                           CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
        return context.sample(result, weights, costAlgo, random, timer);
    }

    public static final class Context {
        private final EdgeSwitchMutation.Context edgeSwitch;
        private final SubtreeSwapMutation.Context subtree;
        private final EdgeOptimalRelinkMutation.Context relink;
        private final RandomBSTTraversalMutation.Context bst;

        private final long[] timeBudgets = new long[4];
        private int nullMask = 0;

        private Context(Graph weights) {
            edgeSwitch = EdgeSwitchMutation.getInstance().createContext(weights);
            subtree = SubtreeSwapMutation.getInstance().createContext(weights);
            relink = EdgeOptimalRelinkMutation.getInstance().createContext(weights);
            bst = RandomBSTTraversalMutation.getInstance().createContext(weights);
        }

        private BestTreeAlgorithm.Result sample(BestTreeAlgorithm.Result prev, Graph weights,
                                                CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
            while (nullMask != 15) {
                int minNonNull = -1;
                for (int i = 0; i < 4; ++i) {
                    if ((nullMask & (1 << i)) == 0) {
                        if (minNonNull == -1 || timeBudgets[i] < timeBudgets[minNonNull]) {
                            minNonNull = i;
                        }
                    }
                }

                long t0 = System.nanoTime();
                BestTreeAlgorithm.Result result = switch (minNonNull) {
                    case 0 -> EdgeSwitchMutation.getInstance().mutate(prev, weights, edgeSwitch, costAlgo, random, timer);
                    case 1 -> SubtreeSwapMutation.getInstance().mutate(prev, weights, subtree, costAlgo, random, timer);
                    case 2 -> EdgeOptimalRelinkMutation.getInstance().mutate(prev, weights, relink, costAlgo, random, timer);
                    default -> RandomBSTTraversalMutation.getInstance().mutate(prev, weights, bst, costAlgo, random, timer);
                };
                timeBudgets[minNonNull] += (System.nanoTime() - t0);

                if (result == null) {
                    nullMask |= 1 << minNonNull;
                } else {
                    return result;
                }
            }

            return null;
        }

        private void reset() {
            EdgeSwitchMutation.getInstance().resetContext(edgeSwitch);
            SubtreeSwapMutation.getInstance().resetContext(subtree);
            EdgeOptimalRelinkMutation.getInstance().resetContext(relink);
            RandomBSTTraversalMutation.getInstance().resetContext(bst);
            Arrays.fill(timeBudgets, 0L);
            nullMask = 0;
        }
    }
}
