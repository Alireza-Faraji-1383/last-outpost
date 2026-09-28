package dev.exodus;

import dev.exodus.domain.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.*;

public final class MatchManager {
    private static final Logger LOG=LogUtils.getLogger();
    private static AllocationJob job;
    private MatchManager() {}
    private static void log(String s){LOG.info("[Exodus] {}",s);}
    private static Component msg(String s){return Component.literal(s).withStyle(ChatFormatting.AQUA);}

    public static int start(ServerPlayer source, Integer x, Integer z, boolean randomCenter) {
        MinecraftServer server=source.server; ExodusSavedData d=ExodusSavedData.get(server);
        if(d.state!=MatchState.IDLE){source.sendSystemMessage(msg("A match is already "+d.state+"."));return 0;}
        ServerLevel level=source.serverLevel();
        if(level.dimension()==Level.NETHER||level.dimension()==Level.END){source.sendSystemMessage(msg("Exodus cannot start in the Nether or End."));return 0;}
        List<ServerPlayer> players=level.players().stream().filter(p->p.gameMode.getGameModeForPlayer()==GameType.SURVIVAL).toList();
        if(players.isEmpty()){source.sendSystemMessage(msg("No Survival players are available in this dimension."));return 0;}
        if(players.size()>ExodusConfig.MAX_PLAYERS.get()){source.sendSystemMessage(msg("Too many players: "+players.size()+"/"+ExodusConfig.MAX_PLAYERS.get()));return 0;}
        List<ResourceLocation> structures=ExodusConfig.STRUCTURES.get().stream().map(String::valueOf).map(ResourceLocation::new).filter(id->level.getStructureManager().get(id).isPresent()).toList();
        if(structures.isEmpty()&&!ExodusConfig.DEV_FALLBACK.get()){log("Starter base structure not found: exodus:starter_base");source.sendSystemMessage(msg("No configured starter base structure exists. Start aborted."));return 0;}
        int cx=x==null?source.blockPosition().getX():x, cz=z==null?source.blockPosition().getZ():z;
        long seed=new Random().nextLong(); List<ServerPlayer> shuffled=new ArrayList<>(players);Collections.shuffle(shuffled,new Random(seed));
        d.state=MatchState.STARTING;d.matchId=null;d.dimension=level.dimension().location().toString();d.allocationSeed=seed;d.associations.clear();d.pendingPlayers.clear();d.bases.clear();d.names.clear();
        for(ServerPlayer p:shuffled){d.associations.put(p.getUUID(),Association.MATCH_PLAYER);d.names.put(p.getUUID(),p.getGameProfile().getName());}
        for(ServerPlayer p:level.players()) if(!d.associations.containsKey(p.getUUID())){d.associations.put(p.getUUID(),Association.INITIAL_SPECTATOR);d.names.put(p.getUUID(),p.getGameProfile().getName());}
        d.setDirty(); job=new AllocationJob(level,source.getUUID(),shuffled,structures,cx,cz,randomCenter,seed,server.getTickCount());
        log("Starting match allocation with seed "+seed);source.sendSystemMessage(msg("Exodus allocation started for "+players.size()+" player(s)."));return 1;
    }

    public static void tick(MinecraftServer server){
        if(job!=null){job.tick(server);return;} ExodusSavedData d=ExodusSavedData.get(server); if(d.state!=MatchState.RUNNING)return;
        long now=server.getTickCount();for(UUID id:new ArrayList<>(d.pendingPlayers.keySet()))if(d.pendingPlayers.get(id)<=now){d.pendingPlayers.remove(id);d.associations.put(id,Association.AUTO_SPECTATOR);log("Grace period expired for "+d.names.getOrDefault(id,id.toString()));}
        boolean any=d.associations.entrySet().stream().anyMatch(e->e.getValue()==Association.MATCH_PLAYER&&(server.getPlayerList().getPlayer(e.getKey())!=null||d.pendingPlayers.containsKey(e.getKey())));
        if(!any)stop(server,"No active players remained after the grace period."); d.setDirty();
    }

    public static int stop(MinecraftServer server,String reason){ExodusSavedData d=ExodusSavedData.get(server);if(d.state==MatchState.IDLE)return 0;if(job!=null){job=null;d.state=MatchState.IDLE;d.matchId=null;d.associations.clear();d.bases.clear();d.setDirty();log("Starting allocation cancelled.");return 1;}d.state=MatchState.ENDING;cleanup(server,d);d.state=MatchState.IDLE;d.matchId=null;d.startMillis=0;d.pendingPlayers.clear();d.associations.clear();d.bases.clear();d.setDirty();log("Match ended: "+reason);return 1;}
    public static void endWithWinners(MinecraftServer server,List<UUID> winners){
        ExodusSavedData d=ExodusSavedData.get(server);if(d.state!=MatchState.RUNNING)return;
        String names=winners.stream().map(id->d.names.getOrDefault(id,id.toString())).collect(java.util.stream.Collectors.joining(", "));
        Component announcement=winners.isEmpty()?Component.literal("Nobody escaped through the Exodus Teleporter."):Component.literal("Exodus winners: "+names);
        associatedOnlinePlayers(server).forEach(p->p.sendSystemMessage(announcement));
        stop(server,winners.isEmpty()?"The teleporter expired without an escape.":"The teleporter escape succeeded.");
    }
    public static List<ServerPlayer> associatedOnlinePlayers(MinecraftServer server){ExodusSavedData d=ExodusSavedData.get(server);return d.associations.keySet().stream().map(server.getPlayerList()::getPlayer).filter(Objects::nonNull).toList();}
    private static void cleanup(MinecraftServer server,ExodusSavedData d){ServerLevel level=level(server,d.dimension);if(level!=null)restoreBorder(level,d);for(var e:d.associations.entrySet()){ServerPlayer p=server.getPlayerList().getPlayer(e.getKey());boolean survival=PlayerLifecyclePolicy.forceSurvivalOnCleanup(e.getValue());if(p==null){d.pendingReturns.put(e.getKey(),survival);continue;}if(survival)p.setGameMode(GameType.SURVIVAL);if(level!=null)teleportToSpawn(p,level);}}
    private static void teleportToSpawn(ServerPlayer p,ServerLevel level){BlockPos s=level.getSharedSpawnPos();int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,s.getX(),s.getZ());p.teleportTo(level,s.getX()+.5,y,s.getZ()+.5,p.getYRot(),p.getXRot());}
    public static void login(ServerPlayer p){ExodusSavedData d=ExodusSavedData.get(p.server);Boolean survival=d.pendingReturns.remove(p.getUUID());if(survival!=null){ServerLevel l=level(p.server,d.dimension);if(l!=null)teleportToSpawn(p,l);if(survival)p.setGameMode(GameType.SURVIVAL);d.setDirty();return;}if(d.state!=MatchState.RUNNING)return;if(p.serverLevel().dimension().location().toString().equals(d.dimension)){if(d.pendingPlayers.remove(p.getUUID())!=null){d.setDirty();return;}Association a=d.associations.get(p.getUUID());if(a==Association.AUTO_SPECTATOR)p.setGameMode(GameType.SPECTATOR);else if(a==null){d.associations.put(p.getUUID(),Association.AUTO_SPECTATOR);d.names.put(p.getUUID(),p.getGameProfile().getName());p.setGameMode(GameType.SPECTATOR);}d.setDirty();}}
    public static void logout(ServerPlayer p){leaveActive(p);}
    public static void dimensionChanged(ServerPlayer p,ResourceKey<Level> from,ResourceKey<Level> to){ExodusSavedData d=ExodusSavedData.get(p.server);if(d.state!=MatchState.RUNNING)return;String dim=d.dimension;if(from.location().toString().equals(dim)&&!to.location().toString().equals(dim))leaveActive(p);else if(to.location().toString().equals(dim)){if(d.pendingPlayers.remove(p.getUUID())!=null)d.setDirty();else if(d.associations.get(p.getUUID())==Association.AUTO_SPECTATOR){p.setGameMode(GameType.SPECTATOR);d.setDirty();}else if(!d.associations.containsKey(p.getUUID())){d.associations.put(p.getUUID(),Association.AUTO_SPECTATOR);d.names.put(p.getUUID(),p.getGameProfile().getName());p.setGameMode(GameType.SPECTATOR);d.setDirty();}}}
    private static void leaveActive(ServerPlayer p){ExodusSavedData d=ExodusSavedData.get(p.server);if(d.state==MatchState.RUNNING&&d.associations.get(p.getUUID())==Association.MATCH_PLAYER){d.pendingPlayers.put(p.getUUID(),(long)p.server.getTickCount()+ExodusConfig.DISCONNECT_GRACE_SECONDS.get()*20L);d.setDirty();}}
    public static void recover(MinecraftServer server){ExodusSavedData d=ExodusSavedData.get(server);if(d.state!=MatchState.IDLE){ServerLevel l=level(server,d.dimension);if(l!=null)restoreBorder(l,d);d.state=MatchState.IDLE;d.matchId=null;d.associations.clear();d.pendingPlayers.clear();d.bases.clear();d.setDirty();log("Active match found after restart; recovered to IDLE.");}}
    public static boolean isActiveMatchPlayer(ServerPlayer player){ExodusSavedData d=ExodusSavedData.get(player.server);return d.state==MatchState.RUNNING&&d.matchId!=null&&d.associations.get(player.getUUID())==Association.MATCH_PLAYER&&!d.pendingPlayers.containsKey(player.getUUID())&&player.serverLevel().dimension().location().toString().equals(d.dimension);}
    private static ServerLevel level(MinecraftServer s,String id){if(id==null||id.isBlank())return null;return s.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation(id)));}
    private static void saveBorder(ServerLevel l,ExodusSavedData d){WorldBorder b=l.getWorldBorder();d.oldBorderX=b.getCenterX();d.oldBorderZ=b.getCenterZ();d.oldBorderSize=b.getSize();d.oldDamagePerBlock=b.getDamagePerBlock();d.oldSafeZone=b.getDamageSafeZone();d.oldWarningBlocks=b.getWarningBlocks();d.oldWarningTime=b.getWarningTime();}
    private static void restoreBorder(ServerLevel l,ExodusSavedData d){WorldBorder b=l.getWorldBorder();b.setCenter(d.oldBorderX,d.oldBorderZ);b.setSize(d.oldBorderSize);b.setDamagePerBlock(d.oldDamagePerBlock);b.setDamageSafeZone(d.oldSafeZone);b.setWarningBlocks(d.oldWarningBlocks);b.setWarningTime(d.oldWarningTime);}

    public static List<String> playerLines(ServerPlayer viewer){ExodusSavedData d=ExodusSavedData.get(viewer.server);if(d.state==MatchState.IDLE){String dim=viewer.serverLevel().dimension().location().toString();List<String> r=new ArrayList<>();for(ServerPlayer p:viewer.server.getPlayerList().getPlayers()){String role=!p.serverLevel().dimension().location().toString().equals(dim)?"Ignored":p.gameMode.getGameModeForPlayer()==GameType.SURVIVAL?"Player":"Spectator";r.add(role+": "+p.getGameProfile().getName());}return r;}List<String> r=new ArrayList<>();d.associations.forEach((id,a)->r.add(a+": "+d.names.getOrDefault(id,id.toString())+(d.pendingPlayers.containsKey(id)?" (pending)":"")));return r;}
    public static List<String> status(MinecraftServer s){ExodusSavedData d=ExodusSavedData.get(s);List<String> r=new ArrayList<>(List.of("Project Exodus","State: "+d.state,"Players: "+d.associations.values().stream().filter(a->a==Association.MATCH_PLAYER).count(),"Center: X "+d.centerX+" / Z "+d.centerZ,"Border: "+ExodusConfig.BORDER_SIZE.get(),"Seed: "+d.allocationSeed));if(d.startMillis>0)r.add("Runtime: "+formatDuration(System.currentTimeMillis()-d.startMillis));for(PlayerBaseData b:d.bases.values())r.add(b.name()+" -> X "+b.center().getX()+" Z "+b.center().getZ());return r;}
    private static String formatDuration(long ms){long sec=ms/1000;return String.format("%02d:%02d",sec/60,sec%60);}

    private static final class AllocationJob{
        final ServerLevel level;final UUID source;final List<ServerPlayer> players;final List<ResourceLocation> structures;final Random random;final boolean randomCenter;final int baseCx,baseCz;final long began;int cx,cz,index,attempt,round,centerAttempt;final List<BasePoint> chosen=new ArrayList<>();final Map<UUID,BaseCandidate> results=new LinkedHashMap<>();
        AllocationJob(ServerLevel l,UUID s,List<ServerPlayer> p,List<ResourceLocation> st,int x,int z,boolean rc,long seed,long began){level=l;source=s;players=p;structures=st;random=new Random(seed);randomCenter=rc;baseCx=x;baseCz=z;this.began=began;chooseCenter();}
        void chooseCenter(){if(randomCenter){BlockPos s=level.getSharedSpawnPos();int r=ExodusConfig.RANDOM_CENTER_SEARCH_RADIUS.get();cx=s.getX()+random.nextInt(r*2+1)-r;cz=s.getZ()+random.nextInt(r*2+1)-r;}else{cx=baseCx;cz=baseCz;}index=attempt=0;chosen.clear();results.clear();}
        void tick(MinecraftServer server){if(server.getTickCount()-began>ExodusConfig.STARTING_TIMEOUT_SECONDS.get()*20L){abort(server,"Allocation timed out.");return;}for(int n=0;n<ExodusConfig.CHECKS_PER_TICK.get();n++){if(index>=players.size()){commit(server);return;}if(attempt++>=ExodusConfig.MAX_LOCATION_ATTEMPTS.get()){round++;if(round<ExodusConfig.MAX_ALLOCATION_ROUNDS.get()){chooseCenter();continue;}if(randomCenter&&++centerAttempt<ExodusConfig.MAX_RANDOM_CENTER_ATTEMPTS.get()){round=0;chooseCenter();continue;}abort(server,"Could not find a valid base position for "+players.get(index).getGameProfile().getName());return;}int lim=ExodusConfig.MATCH_RADIUS.get()-ExodusConfig.BORDER_SAFE_DISTANCE.get()-13;int x=cx+random.nextInt(lim*2+1)-lim,z=cz+random.nextInt(lim*2+1)-lim;BaseCandidate c=inspect(x,z);if(c!=null&&policy().isValid(c,cx,cz,chosen,oldBases())){chosen.add(new BasePoint(x,z));results.put(players.get(index).getUUID(),c);log("Base position found for "+players.get(index).getGameProfile().getName()+": "+x+" "+c.y()+" "+z);index++;attempt=0;}}}
        BasePlacementPolicy policy(){return new BasePlacementPolicy(ExodusConfig.MATCH_RADIUS.get(),ExodusConfig.BORDER_SAFE_DISTANCE.get(),ExodusConfig.BASE_MIN_DISTANCE.get(),ExodusConfig.MAX_HEIGHT_VARIATION.get());}
        List<BasePoint> oldBases(){List<BasePoint> out=new ArrayList<>();for(String s:ExodusSavedData.get(level.getServer()).placedCenters){String[] p=s.split(",");if(p.length==3&&p[0].equals(level.dimension().location().toString()))out.add(new BasePoint(Integer.parseInt(p[1]),Integer.parseInt(p[2])));}return out;}
        BaseCandidate inspect(int x,int z){int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;boolean liquid=false,solid=true;for(int dx=-12;dx<=12;dx+=4)for(int dz=-12;dz<=12;dz+=4){int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x+dx,z+dz);BlockPos ground=new BlockPos(x+dx,y-1,z+dz);var biome=level.getBiome(ground);if(biome.is(BiomeTags.IS_OCEAN)||biome.is(BiomeTags.IS_RIVER))return null;var state=level.getBlockState(ground);liquid|=!level.getFluidState(ground).isEmpty();solid&=state.isSolidRender(level,ground);min=Math.min(min,y);max=Math.max(max,y);}int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);return new BaseCandidate(x,y,z,max-min,liquid,solid);}
        void commit(MinecraftServer server){ExodusSavedData d=ExodusSavedData.get(server);saveBorder(level,d);d.centerX=cx;d.centerZ=cz;WorldBorder b=level.getWorldBorder();b.setCenter(cx,cz);b.setSize(ExodusConfig.BORDER_SIZE.get());int i=0;for(ServerPlayer p:players){BaseCandidate c=results.get(p.getUUID());ResourceLocation id=structures.isEmpty()?new ResourceLocation("exodus:dev_fallback"):structures.get(random.nextInt(structures.size()));BlockPos origin;BlockPos spawn;if(structures.isEmpty()){origin=new BlockPos(c.x()-4,c.y(),c.z()-4);for(int dx=0;dx<9;dx++)for(int dz=0;dz<9;dz++)level.setBlock(origin.offset(dx,0,dz),Blocks.STONE_BRICKS.defaultBlockState(),3);spawn=new BlockPos(c.x(),c.y()+1,c.z());}else{StructureTemplate t=level.getStructureManager().get(id).orElse(null);if(t==null){abort(server,"Structure disappeared during commit: "+id);return;}Vec3i size=t.getSize();origin=new BlockPos(c.x()-size.getX()/2,c.y(),c.z()-size.getZ()/2);level.getChunk(origin.getX()>>4,origin.getZ()>>4,ChunkStatus.FULL,true);boolean ok=t.placeInWorld(level,origin,origin,new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE),RandomSource.create(d.allocationSeed+i),2);if(!ok){abort(server,"Structure placement failed for "+p.getGameProfile().getName());return;}spawn=findSpawn(origin.offset(ExodusConfig.BASE_PLAYER_OFFSET_X.get(),ExodusConfig.BASE_PLAYER_OFFSET_Y.get(),ExodusConfig.BASE_PLAYER_OFFSET_Z.get()));if(spawn==null){abort(server,"No safe spawn found for "+p.getGameProfile().getName());return;}}d.bases.put(p.getUUID(),new PlayerBaseData(p.getUUID(),p.getGameProfile().getName(),new BlockPos(c.x(),c.y(),c.z()),origin,spawn,id));d.placedCenters.add(level.dimension().location()+","+c.x()+","+c.z());i++;}d.matchId=UUID.randomUUID();for(ServerPlayer p:players){p.getInventory().clearContent();p.getEnderChestInventory().clearContent();BlockPos s=d.bases.get(p.getUUID()).spawn();p.teleportTo(level,s.getX()+.5,s.getY(),s.getZ()+.5,p.getYRot(),p.getXRot());}d.state=MatchState.RUNNING;d.startMillis=System.currentTimeMillis();d.setDirty();job=null;log("Match started successfully.");}
        BlockPos findSpawn(BlockPos preferred){int r=ExodusConfig.SPAWN_SEARCH_RADIUS.get();for(int dy=0;dy<=4;dy++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){BlockPos p=preferred.offset(dx,dy,dz);if(level.getBlockState(p).getCollisionShape(level,p).isEmpty()&&level.getBlockState(p.above()).getCollisionShape(level,p.above()).isEmpty()&&!level.getBlockState(p.below()).getCollisionShape(level,p.below()).isEmpty())return p;}return null;}
        void abort(MinecraftServer server,String why){log("ERROR: "+why);ServerPlayer p=server.getPlayerList().getPlayer(source);if(p!=null)p.sendSystemMessage(msg(why));ExodusSavedData d=ExodusSavedData.get(server);if(d.oldBorderSize>0&&d.centerX!=0)restoreBorder(level,d);d.state=MatchState.IDLE;d.matchId=null;d.associations.clear();d.bases.clear();d.setDirty();job=null;}
    }
}
