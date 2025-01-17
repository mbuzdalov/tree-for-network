package com.github.mbuzdalov.tree4network.mut;

import com.github.mbuzdalov.tree4network.BoundedForest;
import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.algo.BestBSTOverPermutation;
import com.github.mbuzdalov.tree4network.algo.BestTreeAlgorithm;
import com.github.mbuzdalov.tree4network.cost.CostComputationAlgorithm;
import com.github.mbuzdalov.tree4network.util.Timer;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class RandomBSTTraversalMutation implements Mutation<RandomBSTTraversalMutation.Context> {
    private static final RandomBSTTraversalMutation INSTANCE = new RandomBSTTraversalMutation();
    private RandomBSTTraversalMutation() {}

    public static RandomBSTTraversalMutation getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Optimal BST from traversal";
    }

    @Override
    public Context createContext(Graph weights) {
        return new Context(weights.nVertices());
    }

    @Override
    public void resetContext(Context context) {
        context.reset();
    }

    @Override
    public BestTreeAlgorithm.Result mutate(BestTreeAlgorithm.Result result, Graph weights, Context context,
                                           CostComputationAlgorithm costAlgo, RandomGenerator random, Timer timer) {
        context.initPermutation(result.tree(), random);
        if (context.offset == weights.nVertices()) {
            return result;
        } else {
            return context.bst.construct(weights, context.permutation, context.offset, timer);
        }
    }

    public static class Context {
        private final BestBSTOverPermutation bst;
        private final int[] permutation;
        private final int[] previous;
        private int offset;

        private Context(int n) {
            bst = new BestBSTOverPermutation(n);
            permutation = new int[n];
            previous = new int[n];
            Arrays.fill(permutation, -1);
        }

        private void reset() {
            Arrays.fill(permutation, -1);
            Arrays.fill(previous, -1);
        }

        private void initPermutation(BoundedForest f, RandomGenerator rg) {
            int start;
            int n = f.nVertices();
            do {
                start = rg.nextInt(n);
            } while (f.degree(start) == 3);
            offset = 0;
            System.arraycopy(permutation, 0, previous, 0, n);
            fillPermutation(f, start, -1, rg);
            if (offset != n) {
                throw new AssertionError();
            }
            offset = 0;
            while (offset < n && permutation[offset] == previous[offset]) {
                ++offset;
            }
        }

        private void fillPermutation(BoundedForest f, int curr, int parent, RandomGenerator rg) {
            int next1 = -1, next2 = -1;
            int d = f.degree(curr);
            for (int i = 0; i < d; ++i) {
                int next = f.getDestination(curr, i);
                if (next != parent) {
                    if (next1 == -1) {
                        next1 = next;
                    } else {
                        next2 = next;
                    }
                }
            }
            if (next2 != -1 && rg.nextBoolean()) {
                int tmp = next1;
                next1 = next2;
                next2 = tmp;
            }
            if (next1 != -1) {
                fillPermutation(f, next1, curr, rg);
            }
            permutation[offset] = curr;
            ++offset;
            if (next2 != -1) {
                fillPermutation(f, next2, curr, rg);
            }
        }
    }
}
