package dev.exodus.wasteland.domain;

import java.util.ArrayList;
import java.util.List;

public final class ProgressMilestones {
    private ProgressMilestones() {}

    public static List<Integer> crossed(int oldCompleted, int newCompleted, int total) {
        if (oldCompleted < 0 || newCompleted < oldCompleted || total <= 0 || newCompleted > total) {
            throw new IllegalArgumentException("Invalid progress counters");
        }
        int oldPercent = (int) ((long) oldCompleted * 100L / total);
        int newPercent = (int) ((long) newCompleted * 100L / total);
        List<Integer> milestones = new ArrayList<>();
        for (int milestone = ((oldPercent / 10) + 1) * 10; milestone <= newPercent; milestone += 10) {
            milestones.add(milestone);
        }
        return List.copyOf(milestones);
    }
}
