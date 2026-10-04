package dev.exodus.party;

import dev.exodus.*;
import dev.exodus.domain.Association;
import dev.exodus.event.EventManager;
import dev.exodus.map.MatchMapService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Forge boundary for pure party state. All callers run on the server thread. */
public final class PartyService {
    private PartyService(){}
    public static PartyState state(MinecraftServer server){
        var d=ExodusSavedData.get(server);
        if(d.state!=MatchState.RUNNING||d.matchId==null){throw new IllegalStateException("Parties require a running Exodus match.");}
        var s=d.parties;if(s==null||!s.matchId().equals(d.matchId)){s=new PartyState(d.matchId,ExodusConfig.PARTY_CAPACITY.get());d.parties=s;d.setDirty();}return s;
    }
    private static PartyState current(MinecraftServer server){var d=ExodusSavedData.get(server);var s=d.parties;return d.state==MatchState.RUNNING&&s!=null&&s.matchId().equals(d.matchId)?s:null;}
    public static boolean sameParty(MinecraftServer server,UUID a,UUID b){var s=current(server);return s!=null&&s.sameParty(a,b);}
    public static Optional<UUID> teammate(MinecraftServer server,UUID player){var s=current(server);return s==null?Optional.empty():s.teammate(player);}
    private static void eligible(ServerPlayer player){if(!MatchManager.isActiveMatchPlayer(player)||!player.isAlive())throw new IllegalStateException("Only active living match players can use party commands.");}
    private static long now(MinecraftServer server){return ExodusSavedData.get(server).session.elapsedTicks;}
    public static void create(ServerPlayer player){eligible(player);state(player.server).create(player.getUUID());notice(player,"Party created. Use /exodus party invite <player>.");}
    public static void invite(ServerPlayer creator,ServerPlayer target){eligible(creator);eligible(target);state(creator.server).invite(creator.getUUID(),target.getUUID(),now(creator.server),ExodusConfig.PARTY_INVITE_SECONDS.get()*20L);notice(creator,"Party invitation sent to "+name(target)+".");notice(target,name(creator)+" invited you to a party. Use /exodus party accept "+name(creator)+" or /exodus party decline "+name(creator)+" within "+ExodusConfig.PARTY_INVITE_SECONDS.get()+" seconds.");}
    public static void accept(ServerPlayer target,UUID creator){
        eligible(target);var inviter=target.server.getPlayerList().getPlayer(creator);if(inviter==null)throw new IllegalStateException("The inviting player is offline.");eligible(inviter);
        state(target.server).accept(target.getUUID(),creator,now(target.server));
        notice(target,"You joined "+name(inviter)+"'s party.");notice(inviter,name(target)+" joined your party.");
        EventManager.partyChanged(target.server);changed(target.server);
    }
    public static void decline(ServerPlayer target,UUID creator){eligible(target);state(target.server).decline(target.getUUID(),creator);notice(target,"Party invitation declined.");var inviter=target.server.getPlayerList().getPlayer(creator);if(inviter!=null)notice(inviter,name(target)+" declined your party invitation.");}
    public static void leave(ServerPlayer player,boolean disband){
        eligible(player);var s=state(player.server);if(disband&&!s.owner(player.getUUID()).filter(player.getUUID()::equals).isPresent())throw new IllegalStateException("Only the creator can disband this party.");
        UUID mate=s.teammate(player.getUUID()).orElse(null);
        boolean delayed=s.leave(player.getUUID(),now(player.server),ExodusConfig.PARTY_DEPARTURE_SECONDS.get()*20L);
        if(delayed){String message=name(player)+" requested party separation. You remain teammates for "+ExodusConfig.PARTY_DEPARTURE_SECONDS.get()+" seconds; friendly fire is disabled until then.";notice(player,message);var other=mate==null?null:player.server.getPlayerList().getPlayer(mate);if(other!=null)notice(other,message);}
        else{notice(player,"Party disbanded.");changed(player.server);}
    }
    public static void status(ServerPlayer player){
        eligible(player);var s=state(player.server);var owner=s.owner(player.getUUID());
        if(owner.isEmpty()){notice(player,"You do not belong to a party.");return;}
        String ownerName=ExodusSavedData.get(player.server).names.getOrDefault(owner.get(),owner.get().toString());
        var online=player.server.getPlayerList().getPlayer(owner.get());if(online!=null)ownerName=name(online);
        String mate=s.teammate(player.getUUID()).map(id->{var p=player.server.getPlayerList().getPlayer(id);return p==null?ExodusSavedData.get(player.server).names.getOrDefault(id,id.toString()):name(p);}).orElse("None");
        notice(player,"Party creator: "+ownerName+". Teammate: "+mate+".");if(s.departureDeadline(player.getUUID())>=0)notice(player,"Separation in "+Math.max(0,(s.departureDeadline(player.getUUID())-now(player.server)+19)/20)+" seconds.");
    }
    public static Set<UUID> invitations(ServerPlayer player){var s=current(player.server);return s==null?Set.of():s.invitedBy(player.getUUID(),now(player.server));}
    public static void tick(MinecraftServer server){
        var s=current(server);if(s==null)return;var d=ExodusSavedData.get(server);boolean changed=false;
        for(UUID id:s.members())if(d.associations.get(id)!=Association.MATCH_PLAYER||d.session.eliminated.contains(id)){
            var removed=s.remove(id);notifyAll(server,removed,"Party ended because a member left the match.");changed|=!removed.isEmpty();
        }
        for(var ended:s.tick(now(server))){notifyAll(server,ended,"Party separation complete. You can now damage each other.");changed=true;}
        if(changed)changed(server);
    }
    public static void clear(MinecraftServer server){var d=ExodusSavedData.get(server);d.parties=null;d.setDirty();}
    private static void changed(MinecraftServer server){MatchMapService.updateNow(server);dev.exodus.session.MatchBossBarService.tick(server);}
    private static void notifyAll(MinecraftServer server,Set<UUID> members,String message){for(UUID id:members){var p=server.getPlayerList().getPlayer(id);if(p!=null)notice(p,message);}}
    private static void notice(ServerPlayer p,String message){p.sendSystemMessage(Component.literal(message));}
    private static String name(ServerPlayer p){return p.getGameProfile().getName();}
}
