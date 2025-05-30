package com.github.mbuzdalov.tree4network;

import java.util.Arrays;

public final class BoundedSimpleGraph {
    private static final boolean DEBUG_CHECKS = false;

    private final int maxDegreePlusOne;
    private final int[] state; // state[0] is degree, state[1..d] are adjacent vertices
    private int nEdges;

    public BoundedSimpleGraph(int n, int maxDegree) {
        this.maxDegreePlusOne = maxDegree + 1;
        state = new int[maxDegreePlusOne * n];
        Arrays.fill(state, -1);
        for (int i = 0, j = 0; i < n; ++i, j += maxDegreePlusOne) {
            state[j] = 0;
        }
    }

    public BoundedSimpleGraph(BoundedSimpleGraph other) {
        state = other.state.clone();
        maxDegreePlusOne = other.maxDegreePlusOne;
        nEdges = other.nEdges;
    }

    public int maximumDegree() {
        return maxDegreePlusOne - 1;
    }

    public int nVertices() {
        return state.length / maxDegreePlusOne;
    }

    public int nEdges() {
        return nEdges;
    }

    public boolean hasEdge(int a, int b) {
        int ao = a * maxDegreePlusOne;
        int d = state[ao];
        for (int i = 1; i <= d; ++i) {
            if (state[ao + i] == b) {
                return true;
            }
        }
        return false;
    }

    public int degree(int a) {
        return state[a * maxDegreePlusOne];
    }

    public int getDestination(int source, int index) {
        return state[source * maxDegreePlusOne + index + 1];
    }

    public void addEdge(int a, int b) {
        int ao = a * maxDegreePlusOne;
        int bo = b * maxDegreePlusOne;
        checkCanAdd(ao, bo);
        checkHasNoEdge(a, b);
        addOneEdge(ao, b);
        addOneEdge(bo, a);
        ++nEdges;
    }

    public void removeEdge(int a, int b) {
        removeOneEdge(a * maxDegreePlusOne, b);
        removeOneEdge(b * maxDegreePlusOne, a);
        --nEdges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundedSimpleGraph graph = (BoundedSimpleGraph) o;
        if (nEdges != graph.nEdges) {
            return false;
        }
        if (state.length != graph.state.length) {
            return false;
        }
        if (maxDegreePlusOne != graph.maxDegreePlusOne) { // questionable but valid in the current usage
            return false;
        }

        int[] vThis = new int[maxDegreePlusOne - 1];
        int[] vThat = new int[maxDegreePlusOne - 1];

        int nV = nVertices();
        for (int v = 0; v < nV; ++v) {
            int d = degree(v);
            if (d != graph.degree(v)) {
                return false;
            }
            for (int i = 0; i < d; ++i) {
                vThis[i] = getDestination(v, i);
                vThat[i] = graph.getDestination(v, i);
            }
            Arrays.sort(vThis, 0, d);
            Arrays.sort(vThat, 0, d);
            if (!Arrays.equals(vThis, 0, d, vThat, 0, d)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (1 + nEdges) * 37;
        int nV = nVertices();
        int[] adj = new int[maxDegreePlusOne - 1];
        for (int v = 0; v < nV; ++v) {
            int d = degree(v);
            for (int i = 0; i < d; ++i) {
                adj[i] = getDestination(v, i);
            }
            Arrays.sort(adj, 0 ,d);
            for (int i = 0; i < d; ++i) {
                result = 31 * result + adj[i];
            }
        }
        return result;
    }

    private void addOneEdge(int ao, int b) {
        state[ao + ++state[ao]] = b;
    }

    private void removeOneEdge(int ao, int b) {
        int d = state[ao];
        for (int i = 1; i <= d; ++i) {
            if (state[ao + i] == b) {
                state[ao + i] = state[ao + d];
                state[ao + d] = -1;
                --state[ao];
                return;
            }
        }
        throw new IllegalStateException("Removing a non-existing edge");
    }

    private void checkCanAdd(int ao, int bo) {
        if (DEBUG_CHECKS) {
            if (state[ao] == maxDegreePlusOne - 1 || state[bo] == maxDegreePlusOne - 1) {
                throw new IllegalArgumentException("Adding this edge will make degree " + maxDegreePlusOne);
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
