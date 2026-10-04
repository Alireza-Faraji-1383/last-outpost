package dev.exodus.event;

import dev.exodus.ExodusMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID)
public final class EventHooks {
    private EventHooks(){}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event){
        if(event.getEntity() instanceof ServerPlayer player)EventManager.playerDeath(player);
        else if(event.getSource().getEntity() instanceof ServerPlayer killer
                &&killer.serverLevel()==event.getEntity().level())EventManager.entityDeath(killer,event.getEntity().getUUID(),BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString());
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){EventScoreboardService.clear(p);EventScoreboardService.forget(p.server,p.getUUID());}}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)EventScoreboardService.clear(p);}
}
