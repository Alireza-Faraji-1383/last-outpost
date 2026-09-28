package dev.exodus.wasteland.arena;

import java.util.List;

public final class ArenaStatusFormatter {
    private ArenaStatusFormatter() {}
    public static List<String> format(ArenaRecord arena) {
        String location = "center=" + arena.centerX() + "," + arena.centerZ() + " size=" + arena.arenaSize();
        String progress = arena.state() == ArenaState.PREPARING
                ? " phase=" + arena.checkpoint().phase + " progress=" + arena.checkpoint().lastAnnouncedPercent + "%"
                : "";
        String failure = arena.failure().isBlank() ? "" : " reason=" + arena.failure();
        return List.of("Arena " + arena.id() + " " + arena.state() + progress + " " + location + failure);
    }
}
