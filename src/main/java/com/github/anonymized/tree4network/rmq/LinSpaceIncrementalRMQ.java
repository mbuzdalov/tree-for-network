package com.github.anonymized.tree4network.rmq;

public final class LinSpaceIncrementalRMQ extends RangeMinimumQuery {
    private final int[] values;
    private final int blockSize;
    private final int[][] blockPrecomputed;
    private final int[] blockMinValues, blockMinIndices;
    private final int[] blockBitStrings;
    private final int[] blockPrefixes, blockSuffixes;
    private final LinLogSpaceRMQ blockRMQ;

    public LinSpaceIncrementalRMQ(int[] values) {
        this.values = values;
        int logSize = 1;
        while ((1 << logSize) <= values.length) {
            ++logSize;
        }
        blockSize = Math.max(2, logSize);
        int nBlocks = (values.length + blockSize - 1) / blockSize;
        blockMinValues = new int[nBlocks];
        blockMinIndices = new int[nBlocks];
        blockBitStrings = new int[nBlocks];
        blockRMQ = new LinLogSpaceRMQ(blockMinValues);

        blockPrefixes = new int[values.length];
        blockSuffixes = new int[values.length];

        // blockPrecomputed[i] describes solutions for all blocks of size 2 + i
        blockPrecomputed = new int[blockSize - 1][];
        precomputeBlocks();
    }

    private int precomputeOne(int str, int bs) {
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
        return bestI;
    }

    private void precomputeBlocks() {
        for (int bsi = 0; bsi < blockPrecomputed.length; ++bsi) {
            int bs = 1 + bsi;
            int[] bp = blockPrecomputed[bsi] = new int[1 << bs];
            for (int str = 0; str < bp.length; ++str) {
                bp[str] = precomputeOne(str, bs);
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

            blockPrefixes[blockBegin] = blockBegin;
            for (int i = blockBegin + 1; i < blockEnd; ++i) {
                int curr = values[i];
                if (minValue > curr) {
                    minValue = curr;
                    minIndex = i;
                }
                blockPrefixes[i] = minIndex;
                int prev = values[i - 1];
                if (prev + 1 == curr) {
                    stringEncoding |= 1 << (i - 1 - blockBegin);
                }
            }

            blockMinIndices[block] = minIndex;
            blockMinValues[block] = minValue;
            blockBitStrings[block] = stringEncoding;

            int sfIndex = blockEnd - 1;
            int sfValue = values[sfIndex];
            blockSuffixes[sfIndex] = sfIndex;
            for (int i = sfIndex - 1; i >= blockBegin; --i) {
                int curr = values[i];
                if (sfValue > curr) {
                    sfValue = curr;
                    sfIndex = i;
                }
                blockSuffixes[i] = sfIndex;
            }
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
            int fromIndex = blockSuffixes[from];
            int toIndex = blockPrefixes[to];
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
