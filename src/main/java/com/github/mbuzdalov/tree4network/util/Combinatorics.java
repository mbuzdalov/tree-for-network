package com.github.mbuzdalov.tree4network.util;

import java.util.Random;

public final class Combinatorics {
    private Combinatorics() {}

    public static void fillRandomPermutation(int[] permutation, Random random) {
        int n = permutation.length;
        permutation[0] = 0;
        for (int i = 1; i < n; ++i) {
            int j = random.nextInt(i + 1);
            permutation[i] = permutation[j];
            permutation[j] = i;
        }
    }

    public static void fillInverseOrder(int[] permutation, int[] inverse) {
        if (permutation.length != inverse.length) {
            throw new IllegalArgumentException("Array lengths are not equal");
        }
        for (int i = 0; i < permutation.length; ++i) {
            inverse[permutation[i]] = i;
        }
    }

    public static <T> void shufflePart(T[] edges, int from, int until, Random random) {
        for (int i = from + 1; i < until; ++i) {
            int j = random.nextInt(i - from + 1) + from;
            T e = edges[i];
            edges[i] = edges[j];
            edges[j] = e;
        }
    }

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
