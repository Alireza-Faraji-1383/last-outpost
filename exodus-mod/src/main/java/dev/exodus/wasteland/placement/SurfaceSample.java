package dev.exodus.wasteland.placement;

public record SurfaceSample(SurfacePlacementPolicy.Selection selection,
                            int validSamples, int unsupportedSamples) {}
