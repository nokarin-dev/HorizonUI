package com.nokarin.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressTracker {
    private final int totalWeight;
    private final AtomicInteger accumulated = new AtomicInteger(0);

    public ProgressTracker(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public int updateStage(int stageWeight, int stageProgress) {
        int weighted = (stageWeight * stageProgress) / 100;
        accumulated.set(weighted);
        return accumulated.get();
    }

    public int get() {
        return accumulated.get();
    }
}
