package com.github.mbuzdalov.tree4network.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class CombinatoricsTests {
    private void testPermutation(int result, int... input) {
        int n2 = input.length;
        int n = n2 / 2;
        int[] found = Arrays.copyOfRange(input, 0, n);
        int[] expected = Arrays.copyOfRange(input, n, n2);
        int r = Combinatorics.nextPermutation(found);
        Assert.assertEquals(result, r);
        if (result != -1) {
            Assert.assertArrayEquals(expected, found);
        }
    }

    @Test
    public void nextPermutationTests() {
        testPermutation(-1,  0,   -1);
        testPermutation(0,  0, 1,   1, 0);
        testPermutation(-1,  1, 0,   -1, -1);
        testPermutation(3,  0, 1, 2, 3, 4,   0, 1, 2, 4, 3);
        testPermutation(2,  0, 1, 2, 4, 3,   0, 1, 3, 2, 4);
        testPermutation(3,  0, 1, 3, 2, 4,   0, 1, 3, 4, 2);
        testPermutation(0,  0, 4, 3, 2, 1,   1, 0, 2, 3, 4);
        testPermutation(-1,  4, 3, 2, 1, 0,   -1, -1, -1, -1, -1);
    }

    @Test
    public void permutationFactorial() {
        for (int n = 1, f = 1; n <= 10; ++n, f *= n) {
            int nRuns = 0;
            int[] array = new int[n];
            for (int i = 0; i < n; ++i) {
                array[i] = i;
            }
            do {
                ++nRuns;
            } while (Combinatorics.nextPermutation(array) >= 0);

            Assert.assertEquals(f, nRuns);
        }
    }
}
