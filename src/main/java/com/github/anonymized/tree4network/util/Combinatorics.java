package com.github.anonymized.tree4network.util;

import java.util.random.RandomGenerator;

public final class Combinatorics {
    private Combinatorics() {}

    /**
     * Fills the given array with the identity permutation, that is, [0, 1, ..., n - 1].
     * @param permutation the array to be filled with the identity permutation.
     */
    public static void fillIdentityPermutation(int[] permutation) {
        int n = permutation.length;
        for (int i = 0; i < n; ++i) {
            permutation[i] = i;
        }
    }

    /**
     * Fills the given array with a random permutation, using the supplied random number generator.
     * @param permutation the array to be filled with a random permutation.
     * @param random the random number generator to use.
     */
    public static void fillRandomPermutation(int[] permutation, RandomGenerator random) {
        int n = permutation.length;
        permutation[0] = 0;
        for (int i = 1; i < n; ++i) {
            int j = random.nextInt(i + 1);
            permutation[i] = permutation[j];
            permutation[j] = i;
        }
    }

    /**
     * Fills the given array with an inverse of the given permutation.
     * @param permutation the source permutation.
     * @param inverse the array to be filled with the inverse of #permutation.
     */
    public static void fillInverseOrder(int[] permutation, int[] inverse) {
        if (permutation.length != inverse.length) {
            throw new IllegalArgumentException("Array lengths are not equal");
        }
        for (int i = 0; i < permutation.length; ++i) {
            inverse[permutation[i]] = i;
        }
    }

    /**
     * Shuffles a part of the given array using the supplied random number generator.
     * @param array the array to be modified.
     * @param from the first index of the array, inclusive, to be shuffled.
     * @param until the last index of the array, exclusive, to be shuffled.
     * @param random the random number generator to use.
     * @param <T> the type of elements in the array.
     */
    public static <T> void shufflePart(T[] array, int from, int until, RandomGenerator random) {
        for (int i = from + 1; i < until; ++i) {
            int j = random.nextInt(from, i + 1);
            T e = array[i];
            array[i] = array[j];
            array[j] = e;
        }
    }

    /**
     * <p>Given an array of distinct integers, produces the lexicographically next permutation
     * and returns the minimum updated index.</p>
     * <p>If the array is decreasing, reverses it and returns -1.</p>
     *
     * @param permutation the array containing a permutation,
     *                    for which the lexicographically next permutation needs to be created.
     * @return the minimum index that has changed, or -1 if the last permutation was turned into the first permutation.
     */
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
        fillIdentityPermutation(permutation);
        return -1;
    }

    /**
     * <p>Computes factorials to fit in 64-bit integers.</p>
     * <p>For integers 0 to 20, inclusively, returns a factorial of the argument.</p>
     * <p>For bigger integers, returns {@link Long#MAX_VALUE}.</p>
     * <p>For negative integers, throws an {@link IllegalArgumentException}.</p>
     * @param n the integer for which the factorial needs to be computed.
     * @return the factorial, or {@link Long#MAX_VALUE} if the factorial is too large.
     */
    public static long factorialOrMaxLong(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n is negative");
        }
        if (n > 20) {
            return Long.MAX_VALUE;
        }
        long result = 1;
        for (int i = 2; i <= n; ++i) {
            result *= i;
        }
        return result;
    }
}
