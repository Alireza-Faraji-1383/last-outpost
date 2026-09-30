package dev.exodus.supply.menu;

import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import dev.exodus.supply.catalog.SupplyCatalog;
import dev.exodus.supply.domain.SupplyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import dev.exodus.supply.request.SupplyRequestService;
import net.minecraft.server.level.ServerPlayer;

public class SupplyRadioMenu extends AbstractContainerMenu {
    public record Entry(String id,String name,String icon,String cost,int remaining,long cooldownUntil,String contents){}
    private final BlockPos pos; private final List<Entry> entries;
    public SupplyRadioMenu(int id,Inventory inventory,BlockPos pos,List<Entry> entries){super(ExodusSupplyRegistry.SUPPLY_RADIO_MENU.get(),id);this.pos=pos;this.entries=List.copyOf(entries);}
    public static SupplyRadioMenu client(int id,Inventory inventory,FriendlyByteBuf buf){BlockPos pos=buf.readBlockPos();int n=buf.readVarInt();List<Entry> values=new ArrayList<>();for(int i=0;i<n;i++)values.add(new Entry(buf.readUtf(),buf.readUtf(),buf.readUtf(),buf.readUtf(),buf.readInt(),buf.readLong(),buf.readUtf()));return new SupplyRadioMenu(id,inventory,pos,values);}
    public static List<Entry> entries(SupplyRadioBlockEntity radio,dev.exodus.supply.domain.RadioType type,java.util.UUID match,long now){return SupplyCatalog.forType(type).stream().map(d->entry(radio,d,match,now)).toList();}
    private static Entry entry(SupplyRadioBlockEntity r,SupplyDefinition d,java.util.UUID m,long now){var u=r.usage(net.minecraft.resources.ResourceLocation.tryParse(d.quotaGroup()),m);int remaining=d.maxRequests()==-1?-1:Math.max(0,d.maxRequests()-u.acceptedRequests());String cost=d.cost()==null?"Free":d.cost().count()+" "+d.cost().itemId().replace("minecraft:","");return new Entry(d.id(),d.displayName(),d.iconItemId(),cost,remaining,Math.max(now,r.cooldownUntil(m)),d.contents());}
    public static void write(FriendlyByteBuf b,BlockPos pos,List<Entry> entries){b.writeBlockPos(pos);b.writeVarInt(entries.size());for(Entry e:entries){b.writeUtf(e.id());b.writeUtf(e.name());b.writeUtf(e.icon());b.writeUtf(e.cost());b.writeInt(e.remaining());b.writeLong(e.cooldownUntil());b.writeUtf(e.contents());}}
    public List<Entry> entries(){return entries;} public BlockPos pos(){return pos;}
    @Override public boolean stillValid(Player player){return player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&player.level().getBlockEntity(pos) instanceof SupplyRadioBlockEntity;}
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player player,int button){if(player instanceof ServerPlayer sp&&button>=0&&button<entries.size()){boolean accepted=SupplyRequestService.request(sp,pos,entries.get(button).id());if(accepted)sp.closeContainer();return accepted;}return false;}
}
