package dev.exodus.map.client;

import dev.exodus.map.MatchMapSnapshot;
import dev.exodus.map.MatchNameTagState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "exodus", value = Dist.CLIENT)
public final class MatchNameTagClient {
    private static final MatchNameTagState STATE = new MatchNameTagState();
    private MatchNameTagClient() {}

    static void receive(MatchMapSnapshot snapshot) { STATE.receive(snapshot); }
    static void reset() { STATE.reset(); }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderName(RenderNameTagEvent event) {
        var entity = event.getEntity();
        String dimension=entity.level().dimension().location().toString();
        if(entity instanceof Player && STATE.teammate(dimension,entity.getUUID())){
            event.setContent(event.getContent().copy().withStyle(net.minecraft.ChatFormatting.GREEN));
            event.setResult(Event.Result.ALLOW);
        } else if (STATE.hidden(dimension,
                entity instanceof Player, entity == Minecraft.getInstance().player,entity.getUUID())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
