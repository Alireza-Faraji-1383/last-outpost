package dev.exodus.teleporter;
import dev.exodus.*;import dev.exodus.teleporter.blockentity.ExodusTeleporterBlockEntity;import dev.exodus.teleporter.domain.*;import dev.exodus.teleporter.item.*;import net.minecraft.core.BlockPos;import net.minecraft.server.level.*;import net.minecraft.world.Container;import net.minecraft.world.entity.item.ItemEntity;import net.minecraft.world.entity.player.Inventory;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;import net.minecraftforge.event.TickEvent;import net.minecraftforge.event.entity.player.PlayerContainerEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.common.Mod;import java.util.*;
@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID)
public final class ComponentLifecycleEvents{
 private static final String SAFE="ExodusSafe";
 @SubscribeEvent public static void playerTick(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||!MatchManager.isActiveMatchPlayer(p))return;ExodusSavedData data=ExodusSavedData.get(p.server);claimAndClean(p.getInventory(),p,data);ejectForeignMenu(p,p.containerMenu);
 }
 @SubscribeEvent public static void closeContainer(PlayerContainerEvent.Close e){if(e.getEntity() instanceof ServerPlayer p&&MatchManager.isActiveMatchPlayer(p))ejectForeignMenu(p,e.getContainer());}
 private static void ejectForeignMenu(ServerPlayer p,AbstractContainerMenu menu){boolean warned=false;for(Slot slot:menu.slots){Container owner=slot.container;if(!ComponentStoragePolicy.shouldEject(owner instanceof Inventory,owner instanceof ExodusTeleporterBlockEntity,owner instanceof ResultContainer))continue;ItemStack stack=slot.getItem();if(stack.getItem() instanceof TeleporterComponentItem){slot.set(ItemStack.EMPTY);if(!p.getInventory().add(stack))p.drop(stack,false);warned=true;}}if(warned)p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Teleporter components cannot be stored in containers."));}
 private static void claimAndClean(Inventory inv,ServerPlayer p,ExodusSavedData data){
  for(int i=0;i<inv.getContainerSize();i++){
   ItemStack stack=inv.getItem(i);
   if(!(stack.getItem() instanceof TeleporterComponentItem item))continue;
   var action=ComponentAcquisitionPolicy.action(ComponentStacks.matchId(stack)!=null,item.component().rare(),data.teleporter.rareClaims().contains(item.component()));
   switch(action){
    case KEEP -> {}
    case BIND -> ComponentStacks.bind(stack,data.matchId);
    case CLAIM_RARE -> {data.teleporter.rareClaims().add(item.component());ComponentStacks.bind(stack,data.matchId);data.setDirty();}
    case DELETE -> {stack.setCount(0);p.sendSystemMessage(net.minecraft.network.chat.Component.literal("That unique teleporter component was already claimed for this match."));}
   }
  }
 }
 @SubscribeEvent public static void levelTick(TickEvent.LevelTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.level instanceof ServerLevel level))return;ExodusSavedData match=ExodusSavedData.get(level.getServer());UUID active=match.matchId;boolean inMatch=active!=null&&match.state==MatchState.RUNNING&&level.dimension().location().toString().equals(match.dimension);for(var entity:level.getAllEntities()){if(!(entity instanceof ItemEntity item)||!(item.getItem().getItem() instanceof TeleporterComponentItem))continue;UUID bound=ComponentStacks.matchId(item.getItem());if(bound==null&&inMatch&&!((TeleporterComponentItem)item.getItem().getItem()).component().rare()){ComponentStacks.bind(item.getItem(),active);bound=active;}if(DroppedComponentPolicy.action(bound,bound!=null||inMatch?active:null)==DroppedComponentPolicy.Action.REMOVE){item.discard();continue;}item.setGlowingTag(true);item.setUnlimitedLifetime();item.setInvulnerable(true);var data=item.getPersistentData();if(item.onGround()){BlockPos pos=item.blockPosition();data.putBoolean(SAFE,true);data.putLong(SAFE+"Pos",pos.asLong());}if(DroppedComponentPolicy.shouldReturnFromVoid(item.getY(),level.getMinBuildHeight(),data.getBoolean(SAFE))){BlockPos pos=BlockPos.of(data.getLong(SAFE+"Pos"));item.teleportTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);item.setDeltaMovement(0,0,0);}}}
}
