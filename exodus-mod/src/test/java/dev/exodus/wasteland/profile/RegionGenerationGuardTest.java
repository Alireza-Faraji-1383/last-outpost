package dev.exodus.wasteland.profile;

import java.io.RandomAccessFile;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RegionGenerationGuardTest {
    @TempDir Path directory;
    private void generated(int x, int z) throws Exception {
        try (var file = new RandomAccessFile(directory.resolve("r." + Math.floorDiv(x,32) + "." + Math.floorDiv(z,32) + ".mca").toFile(), "rw")) {
            file.setLength(8192);
            file.seek(4L * (Math.floorMod(x,32) + Math.floorMod(z,32) * 32));
            file.writeInt(0x00000201);
        }
    }
    @Test void detectsOnlyGeneratedChunksInsideBoundsIncludingNegativeCoordinates() throws Exception {
        assertFalse(RegionGenerationGuard.hasGeneratedChunks(directory, -2,-2,2,2));
        generated(20,20);
        assertFalse(RegionGenerationGuard.hasGeneratedChunks(directory, -2,-2,2,2));
        generated(-1,-1);
        assertTrue(RegionGenerationGuard.hasGeneratedChunks(directory, -2,-2,2,2));
        assertFalse(RegionGenerationGuard.hasGeneratedChunks(directory, 0,0,2,2));
    }
    @Test void corruptRegionDoesNotPassAsFresh() throws Exception {
        java.nio.file.Files.write(directory.resolve("r.0.0.mca"), new byte[]{1,2});
        assertThrows(java.io.IOException.class, () -> RegionGenerationGuard.hasGeneratedChunks(directory,0,0,1,1));
    }
}
