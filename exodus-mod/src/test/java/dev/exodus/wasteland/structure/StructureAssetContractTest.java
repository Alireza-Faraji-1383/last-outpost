package dev.exodus.wasteland.structure;

import java.io.InputStream;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureAssetContractTest {
    @Test
    void factionPiecesAndCampsHaveTheApprovedSizes() throws Exception {
        for (String nation : List.of("russian", "american")) {
            assertSize(nation + "_base_1", 29, 20, 30);
            assertSize(nation + "_base_2", 28, 20, 30);
            assertSize(nation + "_base_3", 29, 20, 30);
            assertSize(nation + "_base_4", 28, 20, 30);
        }
        assertSize("abandoned_camp_01", 11, 11, 16);
        assertSize("occupied_camp_01", 11, 11, 16);
    }

    @Test
    void playerBaseVariantsExistAndOccupiedCampContainsEntities() throws Exception {
        assertPositiveSize(read("starter_base"));
        assertPositiveSize(read("starter_base_2"));
        assertFalse(read("occupied_camp_01").getList("entities", Tag.TAG_COMPOUND).isEmpty(),
                "occupied_camp_01 must be saved with entities enabled");
    }

    @Test
    void eachFactionPartOneContainsTwoEliteMarkers() throws Exception {
        assertTrue(countMetadata(read("russian_base_1"), "exodus:loot/general/elite") == 2);
        assertTrue(countMetadata(read("american_base_1"), "exodus:loot/general/elite") == 2);
    }

    private static long countMetadata(CompoundTag structure, String metadata) {
        return structure.getList("blocks", Tag.TAG_COMPOUND).stream()
                .map(tag -> ((CompoundTag) tag).getCompound("nbt"))
                .filter(tag -> metadata.equals(tag.getString("metadata")))
                .count();
    }

    private static void assertSize(String id, int x, int y, int z) throws Exception {
        assertArrayEquals(new int[]{x, y, z}, read(id).getList("size", Tag.TAG_INT).stream()
                .mapToInt(tag -> ((net.minecraft.nbt.IntTag) tag).getAsInt()).toArray(), id);
    }

    private static void assertPositiveSize(CompoundTag structure) {
        int[] size = structure.getList("size", Tag.TAG_INT).stream()
                .mapToInt(tag -> ((net.minecraft.nbt.IntTag) tag).getAsInt()).toArray();
        assertTrue(size.length == 3 && size[0] > 0 && size[1] > 0 && size[2] > 0);
    }

    private static CompoundTag read(String id) throws Exception {
        String path = "/data/exodus/structures/" + id + ".nbt";
        try (InputStream stream = StructureAssetContractTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return NbtIo.readCompressed(stream);
        }
    }
}
