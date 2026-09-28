package dev.exodus.teleporter.blockentity;
import dev.exodus.teleporter.ExodusTeleporterRegistry;
import dev.exodus.teleporter.menu.ExodusTeleporterMenu;
import net.minecraft.core.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;import net.minecraft.world.level.block.state.BlockState;import java.util.UUID;
public final class ExodusTeleporterBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
 private NonNullList<ItemStack> items=NonNullList.withSize(9,ItemStack.EMPTY);private UUID matchId;private boolean locked;
 public ExodusTeleporterBlockEntity(BlockPos pos,BlockState state){super(ExodusTeleporterRegistry.TELEPORTER_ENTITY.get(),pos,state);}
 @Override protected Component getDefaultName(){return Component.literal("Exodus Teleporter");}
 @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new ExodusTeleporterMenu(id,inv,this);}
 @Override public int getContainerSize(){return 9;}@Override protected NonNullList<ItemStack> getItems(){return items;}@Override protected void setItems(NonNullList<ItemStack> value){items=value;}
 public UUID matchId(){return matchId;}public void matchId(UUID id){matchId=id;setChanged();}public boolean locked(){return locked;}public void lock(){locked=true;setChanged();}public int installed(){return (int)items.stream().filter(s->!s.isEmpty()).count();}
 public void reset(){items=NonNullList.withSize(9,ItemStack.EMPTY);matchId=null;locked=false;setChanged();}
 @Override public int[] getSlotsForFace(Direction side){return new int[0];}@Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return false;}@Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return false;}
 @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);ContainerHelper.saveAllItems(tag,items);if(matchId!=null)tag.putUUID("matchId",matchId);tag.putBoolean("locked",locked);}
 @Override public void load(CompoundTag tag){super.load(tag);items=NonNullList.withSize(9,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items);matchId=tag.hasUUID("matchId")?tag.getUUID("matchId"):null;locked=tag.getBoolean("locked");}
}
