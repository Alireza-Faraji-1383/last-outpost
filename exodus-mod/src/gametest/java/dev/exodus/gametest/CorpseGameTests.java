package dev.exodus.gametest;
import dev.exodus.*;
import dev.exodus.compat.CorpseComponentIntegration;
import dev.exodus.teleporter.ExodusTeleporterRegistry;
import dev.exodus.teleporter.domain.TeleporterComponent;
import dev.exodus.teleporter.item.ComponentStacks;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.*;
@GameTestHolder("exodus_corpse")
@PrefixGameTestTemplate(false)
public final class CorpseGameTests {
 @GameTest(template="smoke",timeoutTicks=100)
 public static void componentsLeaveCorpseOnce(GameTestHelper h) throws Exception {
  if(!net.minecraftforge.fml.ModList.get().isLoaded("corpse")){h.succeed();return;}
  var level=h.getLevel();var data=ExodusSavedData.get(level.getServer());
  var oldState=data.state;var oldId=data.matchId;
  data.state=MatchState.RUNNING;data.matchId=UUID.randomUUID();
  var player=FakePlayerFactory.getMinecraft(level);player.getInventory().clearContent();
  player.setPos(h.absolutePos(net.minecraft.core.BlockPos.ZERO).getCenter());
  var key=new ItemStack(ExodusTeleporterRegistry.item(TeleporterComponent.DIMENSIONAL_CORE).get());ComponentStacks.bind(key,data.matchId);
  player.getInventory().setItem(0,key);player.getInventory().setItem(1,new ItemStack(Items.DIAMOND,3));
  Class<?> deathType=Class.forName("de.maxhenkel.corpse.corelib.death.Death");
  Object death=deathType.getMethod("fromPlayer",Player.class).invoke(null,player);
  Entity corpse=(Entity)Class.forName("de.maxhenkel.corpse.entities.CorpseEntity").getMethod("createFromDeath",Player.class,deathType).invoke(null,player,death);
  player.getInventory().clearContent();
  var saved=new net.minecraft.nbt.CompoundTag();corpse.save(saved);
  try {
   level.addFreshEntity(corpse);CorpseComponentIntegration.recover(corpse,level);CorpseComponentIntegration.recover(corpse,level);
   @SuppressWarnings("unchecked") var inv=(List<ItemStack>)deathType.getMethod("getMainInventory").invoke(death);
   h.assertTrue(inv.get(0).isEmpty(),"Core leaves corpse inventory");h.assertTrue(inv.get(1).is(Items.DIAMOND)&&inv.get(1).getCount()==3,"Ordinary loot is preserved");
   int count=0;for(Entity entity:level.getAllEntities())if(entity instanceof ItemEntity item&&ComponentStacks.isCurrent(item.getItem(),data.matchId)){
    count+=item.getItem().getCount();h.assertTrue(item.isCurrentlyGlowing()&&item.isInvulnerable(),"Drop is protected and glowing");item.discard();
   }
   h.assertTrue(count==1,"Repeated recovery creates exactly one unique core");
   Entity loaded=net.minecraft.world.entity.EntityType.loadEntityRecursive(saved,level,e->e);
   h.assertTrue(loaded!=null,"Stored corpse reloads");loaded.setUUID(UUID.randomUUID());level.addFreshEntity(loaded);
   try {
    Object loadedDeath=loaded.getClass().getMethod("getDeath").invoke(loaded);
    @SuppressWarnings("unchecked") var loadedInv=(List<ItemStack>)deathType.getMethod("getMainInventory").invoke(loadedDeath);
    h.assertTrue(loadedInv.get(0).isEmpty(),"Loading an old corpse recovers its core");
    var stale=key.copy();ComponentStacks.bind(stale,UUID.randomUUID());loadedInv.set(0,stale);CorpseComponentIntegration.recover(loaded,level);
    h.assertTrue(loadedInv.get(0).isEmpty(),"Stale components are removed instead of being reissued");
    int recovered=0;for(Entity entity:level.getAllEntities())if(entity instanceof ItemEntity item&&ComponentStacks.isCurrent(item.getItem(),data.matchId)){recovered+=item.getItem().getCount();item.discard();}
    h.assertTrue(recovered==1,"Old corpse releases one current core without stale duplicates");
   } finally {loaded.discard();}
   h.succeed();
  } finally {corpse.discard();data.state=oldState;data.matchId=oldId;}
 }
}
