package dev.exodus.teleporter.domain;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class TeleporterInventoryPolicyTest {
 @Test void insertionRequiresExactCurrentComponentAndUnlockedDevice(){UUID id=UUID.randomUUID();assertTrue(TeleporterInventoryPolicy.mayInsert(0,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(id,false),id,false));assertFalse(TeleporterInventoryPolicy.mayInsert(1,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(id,false),id,false));assertFalse(TeleporterInventoryPolicy.mayInsert(0,TeleporterComponent.REINFORCED_FRAME,new ComponentStackState(id,false),id,true));}
 @Test void removalStopsAfterActivation(){assertTrue(TeleporterInventoryPolicy.mayRemove(false));assertFalse(TeleporterInventoryPolicy.mayRemove(true));}
}
