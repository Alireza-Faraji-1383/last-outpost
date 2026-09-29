package dev.exodus.domain;

public record BaseCandidate(int x, int y, int z, int heightVariation, boolean hasLiquid,
                            boolean solidSurface, boolean overheadClear) {}
