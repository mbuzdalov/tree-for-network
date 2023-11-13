package com.github.mbuzdalov.tree4network;

public final class BoundedForest {
    private static final boolean DEBUG_CHECKS = true;

    private final int[] state; // state[0] is degree, state[1..3] are adjacent vertices
    private int nEdges;

    public BoundedForest(int n) {
        state = new int[4 * n];
        for (int i = 0, io = -1; i < n; ++i) {
            state[++io] = 0;
            state[++io] = -1;
            state[++io] = -1;
            state[++io] = -1;
        }
    }

    public BoundedForest(BoundedForest other) {
        state = other.state.clone();
        nEdges = other.nEdges;
    }

    public int nVertices() {
        return state.length >>> 2;
    }

    public int nEdges() {
        return nEdges;
    }

    public boolean hasEdge(int a, int b) {
        int ao = a << 2;
        int d = state[ao];
        for (int i = 1; i <= d; ++i) {
            if (state[ao + i] == b) {
                return true;
            }
        }
        return false;
    }

    public int degree(int a) {
        return state[a << 2];
    }

    public int getDestination(int source, int index) {
        return state[(source << 2) + index + 1];
    }

    public void addEdge(int a, int b) {
        int ao = a << 2;
        int bo = b << 2;
        checkCanAdd(ao, bo);
        checkHasNoEdge(a, b);
        addOneEdge(ao, b);
        addOneEdge(bo, a);
        ++nEdges;
    }

    public void removeEdge(int a, int b) {
        removeOneEdge(a << 2, b);
        removeOneEdge(b << 2, a);
        --nEdges;
    }

    private void addOneEdge(int ao, int b) {
        state[ao + ++state[ao]] = b;
    }

    private void removeOneEdge(int ao, int b) {
        int d = state[ao];
        for (int i = 1; i <= d; ++i) {
            if (state[ao + i] == b) {
                state[ao + i] = state[ao + d];
                if (DEBUG_CHECKS) {
                    state[ao + d] = -1; // can drop this once everything works
                }
                --state[ao];
                return;
            }
        }
        throw new IllegalStateException("Removing a non-existing edge");
    }

    private void checkCanAdd(int ao, int bo) {
        if (DEBUG_CHECKS) {
            if (state[ao] == 3 || state[bo] == 3) {
                throw new IllegalArgumentException("Adding this edge will make degree 4");
            }
        }
    }

    private void checkHasNoEdge(int a, int b) {
        if (DEBUG_CHECKS) {
            if (hasEdge(a, b)) {
                throw new IllegalArgumentException("This edge already exists");
            }
        }
    }
}
