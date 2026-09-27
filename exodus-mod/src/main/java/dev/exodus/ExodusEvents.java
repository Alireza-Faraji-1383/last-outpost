package dev.exodus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ExodusEvents {
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){ExodusCommands.register(e.getDispatcher());}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){if(e.phase==TickEvent.Phase.END)MatchManager.tick(e.getServer());}
    @SubscribeEvent public static void started(ServerStartedEvent e){MatchManager.recover(e.getServer());}
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){ExodusSavedData.get(e.getServer()).setDirty();}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)MatchManager.login(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)MatchManager.logout(p);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)MatchManager.dimensionChanged(p,e.getFrom(),e.getTo());}
}
