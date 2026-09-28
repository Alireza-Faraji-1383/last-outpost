package dev.exodus;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExodusSavedDataTest {
    @Test void oldSaveWithoutTeleporterFieldsLoadsEmptyState(){
        ExodusSavedData data=ExodusSavedData.load(new CompoundTag());
        assertNull(data.teleporter.active());
        assertTrue(data.teleporter.rareClaims().isEmpty());
        assertTrue(data.teleporter.invalidatedMatches().isEmpty());
    }
}
