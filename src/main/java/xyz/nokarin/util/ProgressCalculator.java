package xyz.nokarin.util;

import java.util.EnumMap;

public class ProgressCalculator {
    private final EnumMap<UpdateStage, Integer> stageProgress = new EnumMap<>(UpdateStage.class);

    public ProgressCalculator() {
        for (UpdateStage stage : UpdateStage.values()) {
            stageProgress.put(stage, 0);
        }
    }

    public void update(UpdateStage stage, int percent) {
        stageProgress.put(stage, percent);
    }

    public int getTotalProgress() {
        int total = 0;

        for (UpdateStage stage : UpdateStage.values()) {
            int stagePercent = stageProgress.get(stage);
            total += (stage.weight() * stagePercent) / 100;
        }

        return total;
    }
}
