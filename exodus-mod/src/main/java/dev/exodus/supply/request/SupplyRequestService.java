package dev.exodus.supply.request;

import dev.exodus.*;
import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.supply.block.SupplyRadioBlock;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import dev.exodus.supply.blockentity.DropBeaconBlockEntity;
import dev.exodus.supply.catalog.SupplyCatalog;
import dev.exodus.supply.domain.RadioUsagePolicy;
import dev.exodus.supply.entity.SupplyDropEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class SupplyRequestService {
    private SupplyRequestService(){}
    public static boolean request(ServerPlayer player,BlockPos radioPos,String supplyId){
        if(!MatchManager.isActiveMatchPlayer(player))return fail(player,"No active Exodus match.");
        if(!(player.serverLevel().getBlockEntity(radioPos) instanceof SupplyRadioBlockEntity radio)||!player.getUUID().equals(radio.ownerId()))return fail(player,"You do not own this Supply Radio.");
        var definition=SupplyCatalog.get(supplyId).orElse(null);if(definition==null||!definition.enabled())return fail(player,"Unknown supply drop.");
        if(!(player.serverLevel().getBlockState(radioPos).getBlock() instanceof SupplyRadioBlock block)||!definition.radioTypes().contains(block.radioType()))return fail(player,"This supply is unavailable on this Radio.");
        ExodusSavedData data=ExodusSavedData.get(player.server);if(radio.beaconPos()==null||!player.serverLevel().dimension().location().toString().equals(radio.beaconDimension())||radio.activeDropId()!=null)return fail(player,"The Radio link is invalid or busy.");
        if(!(player.serverLevel().getBlockEntity(radio.beaconPos()) instanceof DropBeaconBlockEntity beaconEntity)||!radioPos.equals(beaconEntity.radioPos())||!data.matchId.equals(beaconEntity.matchId())||!player.serverLevel().getWorldBorder().isWithinBounds(radio.beaconPos())||Math.sqrt(radioPos.distSqr(radio.beaconPos()))>ExodusConfig.RADIO_LINK_RANGE.get())return fail(player,"The Radio link is invalid or out of range.");
        var quotaId=ResourceLocation.tryParse(definition.quotaGroup());
        var old=radio.usage(quotaId,data.matchId);
        var oldCooldown=radio.usage(SupplyRadioBlockEntity.RADIO_COOLDOWN,data.matchId);
        var decision=RadioUsagePolicy.evaluate(old,data.matchId,definition.maxRequests(),player.serverLevel().getGameTime());
        if(decision==RadioUsagePolicy.Decision.QUOTA_EXHAUSTED)return fail(player,"Request quota exhausted.");
        if(radio.cooldownUntil(data.matchId)>player.serverLevel().getGameTime())return fail(player,"Supply Radio cooldown is active.");
        Item costItem=null;if(definition.cost()!=null){costItem=BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(definition.cost().itemId()));if(count(player,costItem)<definition.cost().count())return fail(player,"Insufficient payment items.");}
        BlockPos beacon=radio.beaconPos();int y=Math.min(player.serverLevel().getMaxBuildHeight()-2,beacon.getY()+ExodusConfig.DROP_SPAWN_HEIGHT.get());if(y<=beacon.getY())return fail(player,"No legal drop spawn height.");
        int cooldown=definition.cooldownSeconds()==0?0:Math.max(definition.cooldownSeconds(),block.radioType()==dev.exodus.supply.domain.RadioType.SPECIAL?ExodusConfig.SPECIAL_RADIO_COOLDOWN_SECONDS.get():ExodusConfig.BASIC_RADIO_COOLDOWN_SECONDS.get());
        UUID dropId=UUID.randomUUID();if(costItem!=null)remove(player,costItem,definition.cost().count());radio.recordAccepted(quotaId,data.matchId,player.serverLevel().getGameTime()+cooldown*20L,dropId);
        SupplyDropEntity entity=new SupplyDropEntity(ExodusSupplyRegistry.SUPPLY_DROP.get(),player.serverLevel());entity.configure(dropId,data.matchId,radioPos,beacon,definition,player.getUUID());entity.setPos(beacon.getX()+.5,y,beacon.getZ()+.5);
        if(!player.serverLevel().addFreshEntity(entity)){radio.rollbackAccepted(quotaId,old,oldCooldown);if(costItem!=null)player.getInventory().add(new ItemStack(costItem,definition.cost().count()));return fail(player,"Supply drop could not be created.");}
        player.sendSystemMessage(Component.literal("Supply drop inbound."));return true;
    }
    private static int count(ServerPlayer p,Item item){return p.getInventory().items.stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    private static void remove(ServerPlayer p,Item item,int amount){for(ItemStack s:p.getInventory().items){if(amount<=0)break;if(s.is(item)){int n=Math.min(amount,s.getCount());s.shrink(n);amount-=n;}}p.getInventory().setChanged();}
    private static boolean fail(ServerPlayer p,String m){p.sendSystemMessage(Component.literal(m));return false;}
}
