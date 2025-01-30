package com.github.anonymized.tree4network.rmq;

import java.util.Random;

public class RMQPerformanceEvaluation {
    private static void testRMQ(String name, RangeMinimumQuery rmq, int size, long seed) {
        long t0 = System.nanoTime();
        rmq.reloadArray(size);
        long t1 = System.nanoTime();
        Random random = new Random(seed);
        long sumQueries = 0;
        int nQueries = 10000000;
        for (int q = 0; q < nQueries; ++q) {
            int a = random.nextInt(size);
            int b = random.nextInt(size);
            sumQueries += rmq.minimumIndex(Math.min(a, b), 1 + Math.max(a, b));
        }
        long t2 = System.nanoTime();
        System.out.printf("%s: init time %.03f, query time %.03f, checksum %d%n",
                name, (double) (t1 - t0) / size, (double) (t2 - t1) / nQueries, sumQueries);
    }

    public static void main(String[] args) {
        Random random = new Random();
        for (int size : new int[] { 100, 1000, 10000, 100000, 1000000 }) {
            int[] array = new int[size];
            LinLogSpaceRMQ log = new LinLogSpaceRMQ(array);
            LinSpaceIncrementalRMQ lin = new LinSpaceIncrementalRMQ(array);
            System.out.println("Size " + size);
            for (int run = 0; run < 5; ++run) {
                array[0] = random.nextInt(12121);
                for (int i = 1; i < size; ++i) {
                    array[i] = array[i - 1] + 2 * random.nextInt(2) - 1;
                }
                long seed = random.nextLong();
                System.out.println("-----------------------------------");
                testRMQ("log", log, size, seed);
                testRMQ("lin", lin, size, seed);
            }
            System.out.println();
        }
    }
}
