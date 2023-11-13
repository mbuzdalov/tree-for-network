package com.github.mbuzdalov.tree4network.util;

public final class Combinatorics {
    private Combinatorics() {}

    public static int nextPermutation(int[] permutation) {
        int n = permutation.length;
        for (int i = n - 2; i >= 0; --i) {
            if (permutation[i] < permutation[i + 1]) {
                int prev = permutation[i];
                int swap = i;
                while (++swap < n) {
                    if (permutation[swap] < prev) {
                        break;
                    }
                }
                permutation[i] = permutation[--swap];
                permutation[swap] = prev;
                for (int l = i, r = n; ++l < --r; ) {
                    int tmp = permutation[l];
                    permutation[l] = permutation[r];
                    permutation[r] = tmp;
                }
                return i;
            }
        }
        return -1;
    }
}
