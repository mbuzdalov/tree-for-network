package com.github.anonymized.tree4network.cost;

import com.github.anonymized.tree4network.BoundedForest;
import com.github.anonymized.tree4network.Graph;
import com.github.anonymized.tree4network.rmq.LinLogSpaceRMQ;

import java.util.Arrays;

public final class RMQCostComputationAlgorithm extends CostComputationAlgorithm {
    private final int[] depths;
    private final int[] vertexIndices;
    private final LinLogSpaceRMQ rmq;

    private BoundedForest tree;
    private int depthArrayIndex;
    private int depth;

    public RMQCostComputationAlgorithm(int maxSize) {
        depths = new int[maxSize * 2];
        rmq = new LinLogSpaceRMQ(depths);
        vertexIndices = new int[maxSize];
    }

    @Override
    public long compute(Graph weights, BoundedForest tree) {
        if (weights.nVertices() != tree.nVertices()) {
            throw new IllegalArgumentException("Graph sizes do not match");
        }
        if (weights.nVertices() > vertexIndices.length) {
            throw new IllegalArgumentException("Graph is too large");
        }

        this.tree = tree;
        depthArrayIndex = 0;
        depth = 0;
        Arrays.fill(vertexIndices, -1);
        dfs(0, -1);
        rmq.reloadArray(depthArrayIndex);

        long result = 0;
        for (int curr = 0; curr < weights.nVertices(); ++curr) {
            int degree = weights.degree(curr);
            int currVI = vertexIndices[curr];
            int currDepth = depths[currVI];
            for (int j = 0; j < degree; ++j) {
                int next = weights.getDestination(curr, j);
                int nextVI = vertexIndices[next];
                if (currVI < nextVI) {
                    int w = weights.getWeight(curr, j);
                    int nextDepth = depths[nextVI];
                    int rmqDepth = depths[rmq.minimumIndex(currVI, nextVI + 1)];
                    int distance = currDepth + nextDepth - 2 * rmqDepth;
                    result += (long) (w) * distance;
                }
            }
        }

        this.tree = null;
        return result;
    }

    private void dfs(int vertex, int parent) {
        if (vertexIndices[vertex] == -1) {
            vertexIndices[vertex] = depthArrayIndex;
        }
        depths[depthArrayIndex] = depth;
        ++depthArrayIndex;

        int degree = tree.degree(vertex);
        for (int i = 0; i < degree; ++i) {
            int next = tree.getDestination(vertex, i);
            if (next != parent) {
                ++depth;
                dfs(next, vertex);
                depths[depthArrayIndex] = --depth;
                ++depthArrayIndex;
            }
        }
    }
}
