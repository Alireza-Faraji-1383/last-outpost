package dev.exodus.wasteland.profile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads Anvil location headers only; never loads or generates Minecraft chunks. */
public final class RegionGenerationGuard {
    private RegionGenerationGuard() {}
    public static boolean hasGeneratedChunks(Path directory, int minX, int minZ, int maxX, int maxZ) throws IOException {
        for (int rx = Math.floorDiv(minX,32); rx <= Math.floorDiv(maxX,32); rx++) {
            for (int rz = Math.floorDiv(minZ,32); rz <= Math.floorDiv(maxZ,32); rz++) {
                Path path = directory.resolve("r." + rx + "." + rz + ".mca");
                if (!Files.exists(path)) continue;
                byte[] header = new byte[4096];
                try (var file = new RandomAccessFile(path.toFile(), "r")) { file.readFully(header); }
                var locations = ByteBuffer.wrap(header);
                for (int x = Math.max(minX,rx*32); x <= Math.min(maxX,rx*32+31); x++) {
                    for (int z = Math.max(minZ,rz*32); z <= Math.min(maxZ,rz*32+31); z++) {
                        if (locations.getInt(4 * (Math.floorMod(x,32) + Math.floorMod(z,32)*32)) != 0) return true;
                    }
                }
            }
        }
        return false;
    }
}
