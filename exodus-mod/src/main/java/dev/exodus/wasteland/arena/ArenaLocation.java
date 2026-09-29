package dev.exodus.wasteland.arena;

import dev.exodus.wasteland.placement.PlacementPlan;

public record ArenaLocation(String placementId, PlacementPlan.Kind kind, int x, int y, int z, int teleportY) {}
