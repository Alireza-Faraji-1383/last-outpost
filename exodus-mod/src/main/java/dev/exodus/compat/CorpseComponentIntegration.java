package dev.exodus.compat;

import com.mojang.logging.LogUtils;
import dev.exodus.ExodusSavedData;
import dev.exodus.MatchState;
import dev.exodus.teleporter.item.ComponentStacks;
import dev.exodus.teleporter.item.TeleporterComponentItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.lang.reflect.Method;
import java.util.List;

/** Optional bridge using Corpse 1.0.23's public, unmapped inventory accessors. */
@Mod.EventBusSubscriber(modid="exodus")
public final class CorpseComponentIntegration {
 private static final String CORPSE="de.maxhenkel.corpse.entities.CorpseEntity";
 private static final String[] INVENTORIES={"getMainInventory","getArmorInventory","getOffHandInventory","getAdditionalItems"};
 private static boolean warned;
 private CorpseComponentIntegration() {}
 @SubscribeEvent public static void join(EntityJoinLevelEvent event) {
  if(event.getLevel() instanceof ServerLevel level) recover(event.getEntity(),level);
 }
 public static void recover(Entity corpse,ServerLevel level) {
  if(!corpse.getClass().getName().equals(CORPSE)) return;
  try {
   Object death=corpse.getClass().getMethod("getDeath").invoke(corpse);
   if(death==null) return;
   var match=ExodusSavedData.get(level.getServer());
   for(String name:INVENTORIES) {
    Method accessor=death.getClass().getMethod(name);
    @SuppressWarnings("unchecked") List<ItemStack> inventory=(List<ItemStack>)accessor.invoke(death);
    InventoryExtraction.move(inventory,ItemStack.EMPTY,
     stack->!stack.isEmpty()&&stack.getItem() instanceof TeleporterComponentItem,
     stack->{
      // Stale/unbound copies cannot become usable loot in another match.
      if(match.state!=MatchState.RUNNING||!ComponentStacks.isCurrent(stack,match.matchId)) return true;
      ItemEntity drop=new ItemEntity(level,corpse.getX(),Math.max(corpse.getY(),level.getMinBuildHeight())+.5,corpse.getZ(),stack);
      drop.setGlowingTag(true);drop.setUnlimitedLifetime();drop.setInvulnerable(true);drop.setDefaultPickUpDelay();
      drop.getPersistentData().putBoolean("ExodusSafe",true);
      drop.getPersistentData().putLong("ExodusSafePos",drop.blockPosition().asLong());
      return level.addFreshEntity(drop);
     });
   }
  } catch(ReflectiveOperationException|ClassCastException exception) {
   if(!warned){warned=true;LogUtils.getLogger().error("Corpse component recovery failed; stored items were preserved",exception);}
  }
 }
}
