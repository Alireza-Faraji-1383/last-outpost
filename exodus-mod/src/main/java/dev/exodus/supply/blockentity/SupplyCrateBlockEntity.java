package dev.exodus.supply.blockentity;

import dev.exodus.ExodusConfig;
import dev.exodus.supply.ExodusSupplyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.UUID;

public class SupplyCrateBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(54,ItemStack.EMPTY);private String name="Supply Crate";private long emptySince=-1;
    public SupplyCrateBlockEntity(BlockPos pos,BlockState state){super(ExodusSupplyRegistry.SUPPLY_CRATE_ENTITY.get(),pos,state);}
    public void configure(String name,ResourceLocation loot,UUID requester){this.name=name;setLootTable(loot,requester==null?0:requester.getMostSignificantBits());setChanged();}
    @Override protected Component getDefaultName(){return Component.literal(name);}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return ChestMenu.sixRows(id,inv,this);}
    @Override public int getContainerSize(){return 54;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> value){items=value;}
    public static void tick(ServerLevel level,BlockPos pos,BlockState state,SupplyCrateBlockEntity crate){if(crate.lootTable!=null)return;if(crate.isEmpty()){if(crate.emptySince<0){crate.emptySince=level.getGameTime();crate.setChanged();}else if(level.getGameTime()-crate.emptySince>=ExodusConfig.EMPTY_CRATE_SECONDS.get()*20L)level.removeBlock(pos,false);}}
    @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag tag){super.saveAdditional(tag);tag.putString("displayName",name);tag.putLong("emptySince",emptySince);if(!trySaveLootTable(tag))ContainerHelper.saveAllItems(tag,items);}
    @Override public void load(net.minecraft.nbt.CompoundTag tag){super.load(tag);name=tag.getString("displayName");emptySince=tag.getLong("emptySince");items=NonNullList.withSize(54,ItemStack.EMPTY);if(!tryLoadLootTable(tag))ContainerHelper.loadAllItems(tag,items);}
}
