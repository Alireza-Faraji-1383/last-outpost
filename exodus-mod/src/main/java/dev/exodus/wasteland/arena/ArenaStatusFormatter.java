package dev.exodus.wasteland.arena;

import java.util.List;

public final class ArenaStatusFormatter {
    private ArenaStatusFormatter() {}
    public static List<String> format(ArenaRecord arena) {
        String location = "center=" + arena.centerX() + "," + arena.centerZ() + " size=" + arena.arenaSize();
        String poi = arena.checkpoint().poiStates.entrySet().stream()
                .filter(entry -> !entry.getValue().verified)
                .findFirst()
                .map(entry -> " poi=" + entry.getKey() + " chunks=" + entry.getValue().chunkCursor
                        + "/" + entry.getValue().totalChunks
                        + (entry.getValue().surfaceSelected ? " y=" + entry.getValue().platformY
                        + " quorum=" + entry.getValue().quorumCount + "/" + entry.getValue().totalColumns : ""))
                .orElse("");
        String progress = arena.state() == ArenaState.PREPARING
                ? " phase=" + arena.checkpoint().phase + " progress=" + arena.checkpoint().lastAnnouncedPercent + "%" + poi
                : "";
        String failure = arena.failure().isBlank() ? "" : " reason=" + arena.failure();
        return List.of("Arena " + arena.id() + " " + arena.state() + progress + " " + location + failure);
    }
}
