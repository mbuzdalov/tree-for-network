package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.random.RandomGenerator;

public final class OptimalMoveMutation implements Mutation<OptimalMoveMutation.Context> {
    private static final OptimalMoveMutation INSTANCE = new OptimalMoveMutation();
    private OptimalMoveMutation() {}

    public static OptimalMoveMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Two optimal move mutation";
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
        private final EdgeOptimalRelinkMutation.Context relink;
        private final RandomBSTTraversalMutation.Context bst;

        private boolean canRelink = true;

        private Context(Graph weights) {
            relink = EdgeOptimalRelinkMutation.getInstance().createContext(weights);
            bst = RandomBSTTraversalMutation.getInstance().createContext(weights);
        }

        private BestTreeAlgorithm.Result sample(BestTreeAlgorithm.Result prev,
                                                CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
            if (canRelink) {
                BestTreeAlgorithm.Result r = EdgeOptimalRelinkMutation.getInstance().mutate(prev, relink, costAlgo, random, timer);
                if (r == null) {
                    canRelink = false;
                } else {
                    return r;
                }
            }
            return RandomBSTTraversalMutation.getInstance().mutate(prev, bst, costAlgo, random, timer);
        }

        private void reset() {
            EdgeOptimalRelinkMutation.getInstance().resetContext(relink);
            RandomBSTTraversalMutation.getInstance().resetContext(bst);
            canRelink = true;
        }
    }
}
