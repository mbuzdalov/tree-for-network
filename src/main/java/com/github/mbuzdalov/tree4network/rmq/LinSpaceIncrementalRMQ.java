package com.github.mbuzdalov.tree4network.rmq;

public final class LinSpaceIncrementalRMQ extends RangeMinimumQuery {
    private final int[] values;
    private final int blockSize;
    private final int[][] blockPrecomputed;
    private final int[] blockMinValues, blockMinIndices;
    private final int[] blockBitStrings;
    private final LinLogSpaceRMQ blockRMQ;

    public LinSpaceIncrementalRMQ(int[] values) {
        this.values = values;
        int logSize = 1;
        while ((1 << logSize) <= values.length) {
            ++logSize;
        }
        blockSize = Math.max(2, logSize / 2);
        int nBlocks = (values.length + blockSize - 1) / blockSize;
        blockMinValues = new int[nBlocks];
        blockMinIndices = new int[nBlocks];
        blockBitStrings = new int[nBlocks];
        blockRMQ = new LinLogSpaceRMQ(blockMinValues);

        // blockPrecomputed[i] describes solutions for all blocks of size 2 + i
        blockPrecomputed = new int[blockSize - 1][];
        precomputeBlocks();
    }

    private void precomputeBlocks() {
        for (int bsi = 0; bsi < blockPrecomputed.length; ++bsi) {
            int bs = 1 + bsi;
            int[] bp = blockPrecomputed[bsi] = new int[1 << bs];
            for (int str0 = 0; str0 < bp.length; ++str0) {
                int str = str0;
                // str is interpreted as a bit string of length bs, where 0 means -1 and 1 means + 1
                int curr = 0;
                int best = 0;
                int bestI = 0;
                for (int t = 0; t < bs; ++t) {
                    curr += (str & 1) * 2 - 1; // str & 1 == 0 => -1, == 1 => +1
                    if (best > curr) {
                        best = curr;
                        bestI = t + 1;
                    }
                    str >>>= 1;
                }
                bp[str0] = bestI;
            }
        }
    }

    @Override
    public void reloadArray(int n) {
        // Iterate over blocks, process each block separately
        int nBlocks = (n + blockSize - 1) / blockSize;
        for (int block = 0; block < nBlocks; ++block) {
            int blockBegin = block * blockSize;
            int blockEnd = Math.min(blockBegin + blockSize, n);

            int stringEncoding = 0;
            int minValue = values[blockBegin];
            int minIndex = blockBegin;
            for (int i = blockBegin + 1; i < blockEnd; ++i) {
                int curr = values[i];
                if (minValue > curr) {
                    minValue = curr;
                    minIndex = i;
                }
                int prev = values[i - 1];
                if (prev + 1 == curr) {
                    stringEncoding |= 1 << (i - 1 - blockBegin);
                } else if (prev - 1 != curr) {
                    throw new IllegalArgumentException("The array is not incremental, this RMQ implementation cannot process it");
                }
            }
            blockMinIndices[block] = minIndex;
            blockMinValues[block] = minValue;
            blockBitStrings[block] = stringEncoding;
        }
        // Reload the block RMQ
        blockRMQ.reloadArray(nBlocks);
    }

    @Override
    public int minimumIndex(int from, int until) {
        int to = until - 1;
        int fromBlock = from / blockSize;
        int toBlock = to / blockSize;
        if (fromBlock == toBlock) {
            if (from == to) {
                return from;
            }
            int mask = blockBitStrings[fromBlock];
            mask >>>= from - fromBlock * blockSize;
            mask &= (1 << (to - from)) - 1;
            return from + blockPrecomputed[to - from - 1][mask];
        } else {
            int fromLocalIndex = from - fromBlock * blockSize;
            int fromIndex = blockSize - fromLocalIndex == 1
                ? from
                : from + blockPrecomputed[blockSize - fromLocalIndex - 2][blockBitStrings[fromBlock] >>> fromLocalIndex];

            int toLocalIndex = to - toBlock * blockSize;
            int toIndex = toLocalIndex == 0
                ? to
                : toBlock * blockSize + blockPrecomputed[toLocalIndex - 1][blockBitStrings[toBlock] & ((1 << toLocalIndex) - 1)];

            int bestIndex = values[fromIndex] <= values[toIndex] ? fromIndex : toIndex;
            if (fromBlock + 1 < toBlock) {
                int midBlock = blockRMQ.minimumIndex(fromBlock + 1, toBlock);
                int midBlockIndex = blockMinIndices[midBlock];
                if (values[midBlockIndex] < values[bestIndex]) {
                    bestIndex = midBlockIndex;
                }
            }

            return bestIndex;
        }
    }
}
