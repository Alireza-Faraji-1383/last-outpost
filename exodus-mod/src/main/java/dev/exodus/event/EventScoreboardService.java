package dev.exodus.event;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import java.util.*;

/** A detached scoreboard sends only to its recipient; server-wide objectives are never changed. */
public final class EventScoreboardService {
    private static final String NAME="exodus_event";
    private static final Map<MinecraftServer,Map<UUID,View>> VIEWS=new WeakHashMap<>();
    private static final class View {
        final Scoreboard board=new Scoreboard();
        final Objective objective=board.addObjective(NAME,ObjectiveCriteria.DUMMY,Component.literal("PROJECT EXODUS"),ObjectiveCriteria.RenderType.INTEGER);
        final Objective previous;
        final ServerPlayer player;
        List<String> lines=List.of();
        View(ServerPlayer player){this.player=player;previous=player.getScoreboard().getDisplayObjective(1);}
    }
    private EventScoreboardService(){}
    public static void show(ServerPlayer player,List<String> lines){
        var views=VIEWS.computeIfAbsent(player.server,k->new HashMap<>());var view=views.get(player.getUUID());
        if(view!=null&&view.player!=player){clear(player);view=null;}
        if(view==null){view=new View(player);views.put(player.getUUID(),view);player.connection.send(new ClientboundSetObjectivePacket(view.objective,0));}
        if(!view.lines.equals(lines)){
            for(String old:view.lines)player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE,NAME,old,0));
            for(int i=0;i<lines.size();i++)player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE,NAME,lines.get(i),lines.size()-i));
            view.lines=List.copyOf(lines);
        }
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1,view.objective));
    }
    public static void clear(ServerPlayer player){
        var views=VIEWS.get(player.server);var view=views==null?null:views.remove(player.getUUID());if(view==null)return;
        player.connection.send(new ClientboundSetObjectivePacket(view.objective,1));
        Objective restore=player.getScoreboard().getDisplayObjective(1);
        if(restore==null&&view.previous!=null&&player.getScoreboard().getObjective(view.previous.getName())==view.previous)restore=view.previous;
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1,restore));
    }
    public static void forget(MinecraftServer server,UUID id){var views=VIEWS.get(server);if(views!=null)views.remove(id);}
    public static void cleanup(MinecraftServer server){for(var p:server.getPlayerList().getPlayers())clear(p);VIEWS.remove(server);}
}
