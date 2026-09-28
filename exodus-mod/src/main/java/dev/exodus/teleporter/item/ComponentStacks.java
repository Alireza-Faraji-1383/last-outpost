package dev.exodus.teleporter.item;

import dev.exodus.teleporter.domain.ComponentStackState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

public final class ComponentStacks {
    private static final String MATCH_ID="ExodusMatchId";
    private ComponentStacks() {}
    public static void bind(ItemStack stack,UUID matchId){writeMatchId(stack.getOrCreateTag(),matchId);}
    public static UUID matchId(ItemStack stack){return readMatchId(stack.getTag());}
    public static void writeMatchId(CompoundTag tag,UUID matchId){tag.putUUID(MATCH_ID,matchId);}
    public static UUID readMatchId(CompoundTag tag){return tag!=null&&tag.hasUUID(MATCH_ID)?tag.getUUID(MATCH_ID):null;}
    public static ComponentStackState state(ItemStack stack){
        UUID id=matchId(stack);boolean rare=stack.getItem() instanceof TeleporterComponentItem item&&item.component().rare();
        return new ComponentStackState(id,id==null&&rare);
    }
    public static boolean isCurrent(ItemStack stack,UUID current){return current!=null&&current.equals(matchId(stack));}
}
