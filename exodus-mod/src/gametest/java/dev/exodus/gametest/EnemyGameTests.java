package dev.exodus.gametest;

import dev.exodus.*;
import dev.exodus.enemy.*;
import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("exodus_enemy")
@PrefixGameTestTemplate(false)
public final class EnemyGameTests {
    @GameTest(template="smoke",batch="enemy",timeoutTicks=200)
    public static void admissionAndZombieBehavior(GameTestHelper helper) {
        var server=helper.getLevel().getServer();
        ServerLevel level=server.getLevel(LostCitiesIntegration.WASTELAND_DIMENSION);
        helper.assertTrue(level!=null,"Wasteland dimension must exist");
        level.getChunk(0,0);
        BlockPos position=new BlockPos(8,250,8);
        for(int x=4;x<=14;x++)for(int z=4;z<=14;z++){
            level.setBlock(new BlockPos(x,249,z),Blocks.STONE.defaultBlockState(),3);
            for(int y=250;y<=253;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        var data=ExodusSavedData.get(server);
        var mobs=new ArrayList<Mob>();
        var pigs=new ArrayList<net.minecraft.world.entity.animal.Pig>();
        java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> capture=event -> {
            if(event.getLevel()==level && event.getEntity() instanceof net.minecraft.world.entity.animal.Pig pig)pigs.add(pig);
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,false,
                net.minecraftforge.event.entity.EntityJoinLevelEvent.class,capture);
        try {
            Mob natural=EntityType.COW.create(level);
            natural.moveTo(8.5,250,8.5,0,0);
            helper.assertTrue(!level.addFreshEntity(natural),"Direct unauthorized mob must be rejected");
            Mob egg=EntityType.COW.create(level);
            egg.moveTo(9.5,250,8.5,0,0);
            egg.finalizeSpawn(level,level.getCurrentDifficultyAt(position),MobSpawnType.SPAWN_EGG,null,null);
            helper.assertTrue(level.addFreshEntity(egg),"Spawn egg test exception must be admitted");
            mobs.add(egg);
            helper.assertTrue(egg.getPersistentData().getBoolean(EnemyTags.ADMIN),"Egg exception must persist");
            // Custom NBT summon bypasses finalizeSpawn, so exercise the wrapped command path.
            int result=server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "execute in lostcities:lostcity run summon minecraft:pig 10 250 8 {NoAI:1b}");
            helper.assertTrue(result>0,"NBT summon administrator exception must succeed");
            // Generated test chunks need not be entity-ticking; capture admission instead of a ticking-section query.
            helper.assertTrue(pigs.size()==1 && pigs.get(0).getPersistentData().getBoolean(EnemyTags.ADMIN),"Summoned mob must carry persistent exception; found "+pigs.size()+" pigs");
            mobs.addAll(pigs);
            data.state=MatchState.RUNNING;data.matchId=UUID.randomUUID();data.dimension=level.dimension().location().toString();
            var zombie=EnemyRegistry.ZOMBIE.get().create(level);
            zombie.moveTo(12.5,250,12.5,0,0);
            EnemyTags.bind(zombie,data.matchId,UUID.randomUUID(),EnemyKind.ZOMBIE);
            zombie.finalizeSpawn(level,level.getCurrentDifficultyAt(position),MobSpawnType.EVENT,null,null);
            helper.assertTrue(level.addFreshEntity(zombie),"Current managed zombie must be admitted");
            mobs.add(zombie);
            zombie.setBaby(true);
            helper.assertTrue(!zombie.isBaby(),"Managed zombie stays adult");
            helper.assertTrue(zombie.getMainHandItem().isEmpty() && !zombie.canPickUpLoot(),"Managed zombie has no random gear");
            helper.assertTrue(zombie.goalSelector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof BreakDoorGoal),"Door-breaking goal must be installed");
            level.setDayTime(6000);
            for(int i=0;i<80;i++)zombie.aiStep();
            helper.assertTrue(!zombie.isOnFire(),"Managed zombie must not ignite in daylight");
            zombie.setSecondsOnFire(3);
            helper.assertTrue(zombie.isOnFire(),"Environmental fire must still apply");
            var stale=EnemyRegistry.ZOMBIE.get().create(level);
            stale.moveTo(13.5,250,13.5,0,0);
            EnemyTags.bind(stale,UUID.randomUUID(),UUID.randomUUID(),EnemyKind.ZOMBIE);
            helper.assertTrue(!level.addFreshEntity(stale),"Stale match zombie must be rejected");
            data.state=MatchState.IDLE;data.matchId=null;
            EnemySpawnService.tick(server);
            helper.assertTrue(zombie.isRemoved(),"Ending match must remove its managed mobs without death");
            helper.assertTrue(!egg.isRemoved(),"Ending match must preserve administrator test mobs");
            helper.succeed();
            com.mojang.logging.LogUtils.getLogger().info("[Exodus Test] Enemy spawn gate, NBT summon, adult/daylight/fire/door goals and cleanup passed.");
        } finally {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(capture);
            mobs.forEach(Entity::discard);
            data.state=MatchState.IDLE;data.matchId=null;data.teleporter.active(null);data.setDirty();
            EnemySpawnService.reset();
        }
    }
}
