package dev.exodus.wasteland.arena;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChunkPreparationCursorTest {
    @Test void coversEveryBufferedChunkExactlyOnceAndResumes() {
        ChunkPreparationCursor cursor = ChunkPreparationCursor.forArea(4096, 0, 2256, 0);
        var seen = new HashSet<String>();
        int total = cursor.total();
        while (!cursor.complete()) {
            assertTrue(seen.add(cursor.chunkX() + "," + cursor.chunkZ()));
            cursor = cursor.advance();
        }
        assertEquals(total, seen.size());
        ChunkPreparationCursor resumed = ChunkPreparationCursor.forArea(4096, 0, 2256, 37);
        assertEquals(37, resumed.index());
    }
}
