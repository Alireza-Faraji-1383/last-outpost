package dev.exodus.teleporter.menu;
import dev.exodus.*;import dev.exodus.teleporter.*;import dev.exodus.teleporter.blockentity.ExodusTeleporterBlockEntity;import dev.exodus.teleporter.domain.*;import dev.exodus.teleporter.item.*;import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;
import java.util.UUID;
public final class ExodusTeleporterMenu extends AbstractContainerMenu {
 private final Container device;private final ExodusTeleporterBlockEntity blockEntity;
 public static ExodusTeleporterMenu client(int id,Inventory inv,FriendlyByteBuf buf){BlockPos pos=buf.readBlockPos();var be=inv.player.level().getBlockEntity(pos);return new ExodusTeleporterMenu(id,inv,be instanceof ExodusTeleporterBlockEntity t?t:new SimpleContainer(9));}
 public ExodusTeleporterMenu(int id,Inventory inv,Container container){super(ExodusTeleporterRegistry.TELEPORTER_MENU.get(),id);device=container;blockEntity=container instanceof ExodusTeleporterBlockEntity t?t:null;checkContainerSize(container,9);container.startOpen(inv.player);
  for(int row=0;row<3;row++)for(int col=0;col<3;col++){int slot=row*3+col;addSlot(new Slot(container,slot,62+col*18,17+row*18){@Override public boolean mayPlace(ItemStack stack){if(!(stack.getItem() instanceof TeleporterComponentItem item))return false;UUID id=ExodusSavedData.get(inv.player.getServer()).matchId;return TeleporterInventoryPolicy.mayInsert(slot,item.component(),ComponentStacks.state(stack),id,blockEntity!=null&&blockEntity.locked());}@Override public boolean mayPickup(Player player){return blockEntity==null||!blockEntity.locked();}@Override public int getMaxStackSize(){return 1;}});}
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,8+col*18,84+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,142));}
 @Override public boolean stillValid(Player player){return device.stillValid(player)&&player instanceof net.minecraft.server.level.ServerPlayer sp&&MatchManager.isActiveMatchPlayer(sp);}
 @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}@Override public void removed(Player player){super.removed(player);device.stopOpen(player);}
}
