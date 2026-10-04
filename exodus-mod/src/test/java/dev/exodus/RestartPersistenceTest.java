package dev.exodus;

import dev.exodus.domain.Association;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RestartPersistenceTest {
    @Test void runningLegacySaveMigratesWithoutDiscardingMatch() {
        ExodusSavedData data = new ExodusSavedData();
        data.state = MatchState.RUNNING;
        data.matchId = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        data.associations.put(player, Association.MATCH_PLAYER);
        data.pendingPlayers.put(player, 9000L);
        data.session.matchId = data.matchId;
        data.session.elapsedTicks = 45000;
        CompoundTag legacy = data.save(new CompoundTag());
        legacy.remove("schemaVersion");
        ExodusSavedData restored = ExodusSavedData.load(legacy);
        assertEquals(MatchState.RUNNING, restored.state);
        assertEquals(data.matchId, restored.matchId);
        assertEquals(45000, restored.session.elapsedTicks);
        assertEquals(Association.MATCH_PLAYER, restored.associations.get(player));
        assertEquals(2, restored.save(new CompoundTag()).getInt("schemaVersion"));
    }
}
