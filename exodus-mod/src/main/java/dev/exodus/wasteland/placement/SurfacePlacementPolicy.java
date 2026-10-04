package dev.exodus.wasteland.placement;

import java.util.Comparator;
import java.util.List;

public final class SurfacePlacementPolicy {
    private SurfacePlacementPolicy() {}

    public static Selection select(List<Integer> supports, int totalColumns, int quorumPercent,
                                   int tolerance, int preferredMinY, int preferredMaxY) {
        if (supports == null || supports.isEmpty()) throw new IllegalArgumentException("Surface supports cannot be empty");
        if (totalColumns <= 0 || supports.size() > totalColumns) throw new IllegalArgumentException("Invalid footprint column count");
        if (quorumPercent < 1 || quorumPercent > 100) throw new IllegalArgumentException("Surface quorum must be between 1 and 100");
        if (tolerance < 0) throw new IllegalArgumentException("Surface tolerance cannot be negative");
        if (preferredMaxY < preferredMinY) throw new IllegalArgumentException("Preferred surface range is invalid");

        int required = (int) Math.ceil(totalColumns * quorumPercent / 100.0);
        Candidate selected = supports.stream().distinct()
                .map(y -> new Candidate(y, countWithin(supports, y, tolerance), countExact(supports, y),
                        y >= preferredMinY && y <= preferredMaxY))
                .filter(candidate -> candidate.count() >= required)
                .max(Comparator.comparingInt(Candidate::count)
                        .thenComparingInt(Candidate::exactCount)
                        .thenComparing(Candidate::preferred)
                        .thenComparingInt(Candidate::y))
                .orElseThrow(NoQuorumException::new);

        int minimum = supports.stream().mapToInt(Integer::intValue).min().orElseThrow();
        int maximum = supports.stream().mapToInt(Integer::intValue).max().orElseThrow();
        return new Selection(selected.y(), selected.count(), totalColumns, minimum, maximum);
    }

    private static int countWithin(List<Integer> supports, int candidateY, int tolerance) {
        return (int) supports.stream().filter(y -> Math.abs((long) y - candidateY) <= tolerance).count();
    }

    private static int countExact(List<Integer> supports, int candidateY) {
        return (int) supports.stream().filter(y -> y == candidateY).count();
    }

    private record Candidate(int y, int count, int exactCount, boolean preferred) {}

    public static final class NoQuorumException extends IllegalStateException {
        public NoQuorumException() {
            super("No surface height satisfies the configured quorum");
        }
    }

    public record Selection(int platformY, int quorumCount, int totalColumns,
                            int minimumSupportY, int maximumSupportY) {
        public int quorumPercent() { return quorumCount * 100 / totalColumns; }
    }
}
