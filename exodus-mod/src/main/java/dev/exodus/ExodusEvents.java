package dev.exodus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import dev.exodus.supply.catalog.SupplyCatalogReloadListener;
import dev.exodus.teleporter.item.*;
import dev.exodus.wasteland.arena.ArenaPreparationService;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ExodusEvents {
    @SubscribeEvent public static void reload(AddReloadListenerEvent e){e.addListener(new SupplyCatalogReloadListener());e.addListener(new dev.exodus.event.EventCatalogReloadListener());}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){ExodusCommands.register(e.getDispatcher());}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){if(e.phase==TickEvent.Phase.END){MatchManager.tick(e.getServer());ArenaPreparationService.tick(e.getServer());dev.exodus.enemy.EnemySpawnService.tick(e.getServer());dev.exodus.horse.HorseSpawnService.tick(e.getServer());}}
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void aboutToStart(ServerAboutToStartEvent e){LostCitiesIntegration.registerDimensionAfterConfigsLoaded();}
    @SubscribeEvent public static void started(ServerStartedEvent e){MatchManager.recover(e.getServer());}
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){dev.exodus.party.PartyService.clear(e.getServer());ExodusSavedData.get(e.getServer()).setDirty();}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){MatchManager.login(p);dev.exodus.map.MatchMapService.updateNow(p.server);}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){MatchManager.logout(p);dev.exodus.map.MatchMapService.updateNow(p.server);}}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){MatchManager.dimensionChanged(p,e.getFrom(),e.getTo());dev.exodus.map.MatchMapService.updateNow(p.server);}}
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getCrafting().getItem() instanceof TeleporterComponentItem){if(MatchManager.isActiveMatchPlayer(p))ComponentStacks.bind(e.getCrafting(),ExodusSavedData.get(p.server).matchId);else{e.getCrafting().setCount(0);p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Teleporter components can only be crafted by active match players."));}}}
}
