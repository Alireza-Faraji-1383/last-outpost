package dev.exodus.wasteland.domain;

import dev.exodus.wasteland.structure.CompositeDefinition;
import java.util.HashSet;
import java.util.Set;

public final class CompositeLayout {
    private CompositeLayout() {}

    public static boolean hasExactCoverage(CompositeDefinition definition) {
        Set<Long> occupied = new HashSet<>();
        for (CompositeDefinition.Part part : definition.parts()) {
            for (int x = part.offsetX(); x < part.offsetX() + part.size().x(); x++) {
                for (int z = part.offsetZ(); z < part.offsetZ() + part.size().z(); z++) {
                    if (!occupied.add(pack(x, z))) {
                        return false;
                    }
                }
            }
        }
        return occupied.size() == definition.total().x() * definition.total().z();
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }
}
