package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public final class RandomChoiceMutation implements Mutation<RandomChoiceMutation.Context> {
    private static final RandomChoiceMutation INSTANCE = new RandomChoiceMutation();
    private RandomChoiceMutation() {}

    public static RandomChoiceMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Random choice of mutation";
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
    public BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Context context,
                                           CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
        return context.sample(result, costAlgo, random, timer);
    }

    public static final class Context {
        private final EdgeSwitchMutation.Context edgeSwitch;
        private final SubtreeSwapMutation.Context subtree;
        private final EdgeOptimalRelinkMutation.Context relink;
        private int nullMask = 0;

        private Context(Graph weights) {
            edgeSwitch = EdgeSwitchMutation.getInstance().createContext(weights);
            subtree = SubtreeSwapMutation.getInstance().createContext(weights);
            relink = EdgeOptimalRelinkMutation.getInstance().createContext(weights);
        }

        private BestTreeAlgorithm.Result sample(BestTreeAlgorithm.Result prev,
                                                CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
            while (nullMask != 7) {
                int index = random.nextInt(3);
                if ((nullMask & (1 << index)) == 0) {
                    BestTreeAlgorithm.Result result = switch (index) {
                        case 0 -> EdgeSwitchMutation.getInstance().mutate(prev, edgeSwitch, costAlgo, random, timer);
                        case 1 -> SubtreeSwapMutation.getInstance().mutate(prev, subtree, costAlgo, random, timer);
                        default -> EdgeOptimalRelinkMutation.getInstance().mutate(prev, relink, costAlgo, random, timer);
                    };
                    if (result != null) {
                        return result;
                    }
                    nullMask |= 1 << index;
                }
            }
            return null;
        }

        private void reset() {
            EdgeSwitchMutation.getInstance().resetContext(edgeSwitch);
            SubtreeSwapMutation.getInstance().resetContext(subtree);
            EdgeOptimalRelinkMutation.getInstance().resetContext(relink);
            nullMask = 0;
        }
    }
}
