package com.github.mbuzdalov.tree4network.util;

public abstract class Timer {
    public abstract boolean shouldInterrupt();
    public abstract long timeConsumedMillis();

    public static Timer newFixedTimer(long startTimeMillis, long timeLimitMillis) {
        return new Timer() {
            public boolean shouldInterrupt() { return timeConsumedMillis() > timeLimitMillis; }
            public long timeConsumedMillis() { return System.currentTimeMillis() - startTimeMillis; }
        };
    }

    private static final Timer DUMMY = new Timer() {
        @Override public boolean shouldInterrupt() { return false; }
        @Override public long timeConsumedMillis() { return 0; }
    };

    public static Timer dummyTimer() {
        return DUMMY;
    }
}
