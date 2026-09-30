package dev.exodus.enemy;

import dev.exodus.*;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.*;

/** Server-thread coordinator. No whole-world scans, chunk tickets, or per-tick target scans. */
public final class EnemySpawnService {
    private static final Logger LOG=LogUtils.getLogger();
    private static final EnemyPopulation POPULATION=new EnemyPopulation();
    private static final Map<UUID, Entry> ENTRIES=new HashMap<>();
    private static final ArrayDeque<UUID> MAINTENANCE=new ArrayDeque<>();
    private static final Set<UUID> RETIRED=new HashSet<>();
    private static final Set<EnemyKind> WARNED=new HashSet<>();
    private static final Map<UUID, Long> NEXT_SPAWN=new HashMap<>();
    private static final Map<UUID, Integer> SCHEDULE_OFFSETS=new HashMap<>();
    private record Opportunity(EnemyKind kind,long worldDay) {}
    private static final Map<UUID, Opportunity> SOLDIER_OPPORTUNITIES=new HashMap<>();
    private static final SoldierSchedule SCHEDULE=new SoldierSchedule(6000,24000);
    private static final EnemyPlacement PLACEMENT=new EnemyPlacement();
    private static UUID currentMatch;
    private static BlockPos device;
    private static int playerCursor;
    private static final class Entry {
        final UUID id, owner, match;
        final EnemyKind kind;
        Mob mob;
        Vec3 lastPosition;
        long farSince=-1;
        Entry(Mob mob) {
            id=mob.getUUID();owner=EnemyTags.owner(mob);match=EnemyTags.match(mob);kind=EnemyTags.kind(mob);
            this.mob=mob;lastPosition=mob.position();
        }
    }
    private EnemySpawnService() {}
    public static boolean retired(UUID id) { return RETIRED.contains(id); }
    public static void join(Mob mob) {
        Entry old=ENTRIES.get(mob.getUUID());
        if (old==null) {
            Entry entry=new Entry(mob);
            ENTRIES.put(entry.id,entry);MAINTENANCE.addLast(entry.id);
            POPULATION.add(entry.id,entry.owner,entry.kind);
        } else { old.mob=mob;old.lastPosition=mob.position(); }
        EnemyCombat.install(mob);
    }
    public static void leave(Mob mob) {
        Entry entry=ENTRIES.get(mob.getUUID());
        if (entry==null) return;
        if (mob.getRemovalReason()==Entity.RemovalReason.UNLOADED_TO_CHUNK) {
            entry.lastPosition=mob.position();entry.mob=null;
        } else {
            ENTRIES.remove(entry.id);POPULATION.remove(entry.id);
        }
    }
    public static BlockPos deviceFor(Mob mob) {
        return currentEnemy(mob) ? device : null;
    }
    public static boolean currentEnemy(Mob mob) {
        return currentMatch!=null && EnemyTags.valid(mob) && currentMatch.equals(EnemyTags.match(mob))
                && mob.level().dimension().equals(LostCitiesIntegration.WASTELAND_DIMENSION);
    }
    public static void tick(MinecraftServer server) {
        ServerLevel level=server.getLevel(LostCitiesIntegration.WASTELAND_DIMENSION);
        if (level==null) return;
        ExodusSavedData data=ExodusSavedData.get(server);
        UUID match=data.state==MatchState.RUNNING && level.dimension().location().toString().equals(data.dimension)?data.matchId:null;
        if (!Objects.equals(currentMatch,match)) {
            currentMatch=match;SCHEDULE.clear();NEXT_SPAWN.clear();SCHEDULE_OFFSETS.clear();SOLDIER_OPPORTUNITIES.clear();device=null;playerCursor=0;
        }
        var active=data.teleporter.active();
        device=match!=null && active!=null && match.equals(active.matchId()) && active.dimension().equals(data.dimension)
                ?BlockPos.of(active.blockPos()):null;
        EnemyAiWork.tick();
        List<ServerPlayer> players=level.players().stream().filter(MatchManager::isActiveMatchPlayer)
                .sorted(Comparator.comparing(ServerPlayer::getUUID)).toList();
        maintain(level,players);
        if (match==null || players.isEmpty()) { SOLDIER_OPPORTUNITIES.clear(); return; }
        long worldDay=Math.floorDiv(level.getDayTime(),24000);
        boolean day=Math.floorMod(level.getDayTime(),24000)<12000;
        SOLDIER_OPPORTUNITIES.entrySet().removeIf(entry -> !day || entry.getValue().worldDay()!=worldDay
                || players.stream().noneMatch(player -> player.getUUID().equals(entry.getKey())));
        // Observe every active owner each tick, even when another allocation gets the placement budget.
        for (ServerPlayer player:players) {
            int offset=SCHEDULE_OFFSETS.computeIfAbsent(player.getUUID(),ignored -> level.random.nextInt(ExodusConfig.SOLDIER_SCHEDULE_WINDOW.get()));
            EnemyKind opportunity=SCHEDULE.due(player.getUUID(),level.getDayTime(),offset);
            if (opportunity!=null) SOLDIER_OPPORTUNITIES.put(player.getUUID(),new Opportunity(opportunity,worldDay));
        }
        PLACEMENT.beginTick();
        // At most one player allocation performs placement work in a server tick.
        ServerPlayer owner=players.get(Math.floorMod(playerCursor++,players.size()));
        UUID id=owner.getUUID();
        Opportunity soldiers=SOLDIER_OPPORTUNITIES.remove(id);
        if (soldiers!=null && day) spawnSoldiers(level,owner,players,soldiers.kind());
        long now=level.getGameTime();
        Long next=NEXT_SPAWN.get(id);
        if (next==null) { NEXT_SPAWN.put(id,now+level.random.nextInt(ExodusConfig.ENEMY_SPAWN_INTERVAL_SECONDS.get()*20)); return; }
        if (now<next) return;
        NEXT_SPAWN.put(id,now+ExodusConfig.ENEMY_SPAWN_INTERVAL_SECONDS.get()*20L);
        boolean nearDevice=device!=null && owner.distanceToSqr(device.getX()+.5,device.getY()+.5,device.getZ()+.5)
                <=square(ExodusConfig.ENEMY_DEVICE_RADIUS.get());
        int zombies=POPULATION.zombies(id);
        var settings=new EnemyPressurePolicy.Settings(ExodusConfig.ENEMY_PLAYER_CAP.get(),ExodusConfig.ENEMY_GLOBAL_CAP.get(),
                ExodusConfig.ZOMBIE_DAY_TARGET.get(),ExodusConfig.ZOMBIE_NIGHT_TARGET.get(),ExodusConfig.ZOMBIE_BATCH.get(),ExodusConfig.DEVICE_ZOMBIE_BATCH.get());
        var pressure=EnemyPressurePolicy.evaluate(day,nearDevice,POPULATION.count(id)-zombies,settings);
        int count=Math.min(pressure.batch(),Math.min(pressure.zombieTarget()-zombies,POPULATION.remaining(id,settings.perPlayerCap(),settings.globalCap())));
        for (int i=0;i<count;i++) {
            Mob mob=create(level,EnemyKind.ZOMBIE);
            if (mob==null) break;
            BlockPos position=PLACEMENT.find(level,owner,players,mob,false,nearDevice?device:null);
            if (position==null) break;
            initialize(level,mob,owner,EnemyKind.ZOMBIE);
            if (!level.addFreshEntity(mob)) break;
        }
    }
    private static void spawnSoldiers(ServerLevel level,ServerPlayer owner,List<ServerPlayer> players,EnemyKind kind) {
        int size=ExodusConfig.SOLDIER_GROUP_SIZE.get();
        if (!POPULATION.canAdd(owner.getUUID(),size,ExodusConfig.ENEMY_PLAYER_CAP.get(),ExodusConfig.ENEMY_GLOBAL_CAP.get())) return;
        List<Mob> group=new ArrayList<>();
        BlockPos anchor=null;
        for (int i=0;i<size;i++) {
            Mob mob=create(level,kind);
            if (mob==null) return;
            BlockPos position=anchor==null?PLACEMENT.find(level,owner,players,mob,true,null):PLACEMENT.findGroupMember(level,owner,players,mob,anchor);
            if (position==null) return;
            if (anchor==null) anchor=position;
            if (group.stream().anyMatch(other -> other.getBoundingBox().intersects(mob.getBoundingBox()))) return;
            group.add(mob);
        }
        for (Mob mob:group) initialize(level,mob,owner,kind);
        List<Mob> added=new ArrayList<>();
        for (Mob mob:group) {
            if (!level.addFreshEntity(mob)) {
                added.forEach(Entity::discard);
                return;
            }
            added.add(mob);
        }
    }
    private static Mob create(ServerLevel level,EnemyKind kind) {
        var type=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(kind.entityId()));
        Entity entity=type==null?null:type.create(level);
        if (entity instanceof Mob mob) return mob;
        if (WARNED.add(kind)) LOG.warn("Exodus enemy type {} is unavailable; its spawn opportunities will be skipped.",kind.entityId());
        return null;
    }
    private static void initialize(ServerLevel level,Mob mob,ServerPlayer owner,EnemyKind kind) {
        EnemyTags.bind(mob,currentMatch,owner.getUUID(),kind);
        mob.finalizeSpawn(level,level.getCurrentDifficultyAt(mob.blockPosition()),MobSpawnType.EVENT,null,null);
        if (mob instanceof ExodusZombie zombie) zombie.configureManagedGoals();
    }
    private static void maintain(ServerLevel level,List<ServerPlayer> players) {
        int work=Math.min(MAINTENANCE.size(),ExodusConfig.ENEMY_WORK_PER_TICK.get());
        long now=level.getGameTime();
        for (int i=0;i<work;i++) {
            UUID id=MAINTENANCE.pollFirst();
            Entry entry=ENTRIES.get(id);
            if (entry==null) continue;
            Mob mob=entry.mob;
            if (!Objects.equals(currentMatch,entry.match)) { retire(entry); continue; }
            if (mob!=null) {
                if (!mob.isAlive()) { ENTRIES.remove(id);POPULATION.remove(id);continue; }
                if (mob.isRemoved()) { entry.lastPosition=mob.position();entry.mob=null;mob=null; }
                else {
                    entry.lastPosition=mob.position();
                    LivingEntity target=mob.getTarget();
                    if (target!=null && (!target.isAlive() || !EnemyCombat.allowed(mob,target,true))) {
                        mob.setTarget(null);mob.getNavigation().stop();
                    }
                }
            }
            double nearest=players.stream().mapToDouble(player -> player.position().distanceToSqr(entry.lastPosition)).min().orElse(Double.POSITIVE_INFINITY);
            if (device!=null) nearest=Math.min(nearest,Vec3.atCenterOf(device).distanceToSqr(entry.lastPosition));
            boolean fighting=mob!=null && mob.getTarget()!=null;
            boolean far=EnemyRangePolicy.farForCleanup(nearest,ExodusConfig.ENEMY_CLEANUP_RADIUS.get(),fighting);
            if (far) {
                if (entry.farSince<0) entry.farSince=now;
                else if (now-entry.farSince>=ExodusConfig.ENEMY_CLEANUP_SECONDS.get()*20L) { retire(entry);continue; }
            } else entry.farSince=-1;
            MAINTENANCE.addLast(id);
        }
    }
    private static void retire(Entry entry) {
        RETIRED.add(entry.id);
        ENTRIES.remove(entry.id);POPULATION.remove(entry.id);
        if (entry.mob!=null && !entry.mob.isRemoved()) entry.mob.discard();
    }
    public static void reset() {
        EnemyAiWork.reset();
        ENTRIES.clear();POPULATION.clear();MAINTENANCE.clear();RETIRED.clear();WARNED.clear();
        NEXT_SPAWN.clear();SCHEDULE_OFFSETS.clear();SOLDIER_OPPORTUNITIES.clear();SCHEDULE.clear();currentMatch=null;device=null;playerCursor=0;
    }
    private static double square(double value) { return value*value; }
}
