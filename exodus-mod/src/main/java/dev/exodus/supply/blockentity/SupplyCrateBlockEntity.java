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
    private UUID eventDropId,eventMatchId;private long eventExpires=Long.MAX_VALUE;private boolean eventCore;
    private NonNullList<ItemStack> items=NonNullList.withSize(54,ItemStack.EMPTY);private String name="Supply Crate";private long emptySince=-1;
    public SupplyCrateBlockEntity(BlockPos pos,BlockState state){super(ExodusSupplyRegistry.SUPPLY_CRATE_ENTITY.get(),pos,state);}
    public void configure(String name,ResourceLocation loot,UUID requester){this.name=name;setLootTable(loot,requester==null?0:requester.getMostSignificantBits());setChanged();}
    public UUID eventDropId(){return eventDropId;}public boolean coreDrop(){return eventCore;}
    public void configureEvent(UUID id,UUID match,long expires,boolean core){
        eventDropId=id;eventMatchId=match;eventExpires=expires;eventCore=core;
        if(core){
            unpackLootTable(null);
            var stack=new ItemStack(dev.exodus.teleporter.ExodusTeleporterRegistry.item(dev.exodus.teleporter.domain.TeleporterComponent.DIMENSIONAL_CORE).get());
            dev.exodus.teleporter.item.ComponentStacks.bind(stack,match);
            int slot=0;while(slot<getContainerSize()-1&&!getItem(slot).isEmpty())slot++;
            if(!getItem(slot).isEmpty()&&level instanceof ServerLevel server)net.minecraft.world.Containers.dropItemStack(server,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,getItem(slot));
            setItem(slot,stack);
        }setChanged();
    }
    @Override protected Component getDefaultName(){return Component.literal(name);}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return ChestMenu.sixRows(id,inv,this);}
    @Override public int getContainerSize(){return 54;}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return false;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> value){items=value;}
    public static void tick(ServerLevel level,BlockPos pos,BlockState state,SupplyCrateBlockEntity crate){
        if(crate.eventDropId!=null){var data=dev.exodus.ExodusSavedData.get(level.getServer());if(data.state!=dev.exodus.MatchState.RUNNING||!java.util.Objects.equals(crate.eventMatchId,data.matchId)||!data.session.events.drops.containsKey(crate.eventDropId)||level.getGameTime()>=crate.eventExpires){level.removeBlock(pos,false);return;}}
        if(crate.lootTable!=null)return;if(crate.isEmpty()){if(crate.emptySince<0){crate.emptySince=level.getGameTime();crate.setChanged();}else if(level.getGameTime()-crate.emptySince>=ExodusConfig.EMPTY_CRATE_SECONDS.get()*20L)level.removeBlock(pos,false);}else if(crate.emptySince>=0){crate.emptySince=-1;crate.setChanged();}
    }
    @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag tag){super.saveAdditional(tag);tag.putString("displayName",name);tag.putLong("emptySince",emptySince);if(eventDropId!=null)tag.putUUID("eventDrop",eventDropId);if(eventMatchId!=null)tag.putUUID("eventMatch",eventMatchId);tag.putLong("eventExpires",eventExpires);tag.putBoolean("eventCore",eventCore);if(!trySaveLootTable(tag))ContainerHelper.saveAllItems(tag,items);}
    @Override public void load(net.minecraft.nbt.CompoundTag tag){super.load(tag);name=tag.getString("displayName");emptySince=tag.contains("emptySince")?tag.getLong("emptySince"):-1;eventDropId=tag.hasUUID("eventDrop")?tag.getUUID("eventDrop"):null;eventMatchId=tag.hasUUID("eventMatch")?tag.getUUID("eventMatch"):null;eventExpires=tag.contains("eventExpires")?tag.getLong("eventExpires"):Long.MAX_VALUE;eventCore=tag.getBoolean("eventCore");items=NonNullList.withSize(54,ItemStack.EMPTY);if(!tryLoadLootTable(tag))ContainerHelper.loadAllItems(tag,items);}
}
