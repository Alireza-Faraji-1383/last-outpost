package dev.exodus.teleporter.item;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ComponentStacksTest {
    @Test void bindingDistinguishesCurrentStaleAndTemplateStacks(){
        UUID current=UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID stale=UUID.fromString("00000000-0000-0000-0000-000000000002");
        CompoundTag tag=new CompoundTag();
        assertNull(ComponentStacks.readMatchId(tag));
        ComponentStacks.writeMatchId(tag,stale);
        assertEquals(stale,ComponentStacks.readMatchId(tag));
        assertNotEquals(current,ComponentStacks.readMatchId(tag));
        ComponentStacks.writeMatchId(tag,current);
        assertEquals(current,ComponentStacks.readMatchId(tag));
    }
}
