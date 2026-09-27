package dev.exodus.supply.blockentity;

import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.supply.domain.RadioUsage;
import dev.exodus.supply.domain.RadioUsagePolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SupplyRadioBlockEntity extends BlockEntity {
    private UUID ownerId, matchId, activeDropId;
    private String ownerName="", beaconDimension="";
    private BlockPos beaconPos;
    private final Map<ResourceLocation, RadioUsage> usage = new HashMap<>();

    public SupplyRadioBlockEntity(BlockPos pos, BlockState state){super(ExodusSupplyRegistry.SUPPLY_RADIO_ENTITY.get(),pos,state);}
    public void claim(UUID match,UUID owner,String name,String dimension,BlockPos beacon){if(!match.equals(matchId)){usage.clear();activeDropId=null;}matchId=match;ownerId=owner;ownerName=name;beaconDimension=dimension;beaconPos=beacon.immutable();setChanged();}
    public UUID ownerId(){return ownerId;} public UUID matchId(){return matchId;} public BlockPos beaconPos(){return beaconPos;} public String beaconDimension(){return beaconDimension;} public UUID activeDropId(){return activeDropId;}
    public RadioUsage usage(ResourceLocation id,UUID match){RadioUsage normalized=RadioUsagePolicy.normalize(usage.get(id),match);usage.put(id,normalized);return normalized;}
    public void recordAccepted(ResourceLocation id,UUID match,long cooldown,UUID drop){usage.put(id,RadioUsagePolicy.accept(usage(id,match),match,cooldown));activeDropId=drop;setChanged();}
    public void rollbackAccepted(ResourceLocation id,RadioUsage previous){usage.put(id,previous);activeDropId=null;setChanged();}
    public void clearInFlight(UUID expected){if(expected!=null&&expected.equals(activeDropId)){activeDropId=null;setChanged();}}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);if(ownerId!=null)tag.putUUID("owner",ownerId);if(matchId!=null)tag.putUUID("match",matchId);if(activeDropId!=null)tag.putUUID("activeDrop",activeDropId);tag.putString("ownerName",ownerName);tag.putString("beaconDimension",beaconDimension);if(beaconPos!=null)tag.putLong("beaconPos",beaconPos.asLong());ListTag list=new ListTag();usage.forEach((id,u)->{CompoundTag e=new CompoundTag();e.putString("id",id.toString());e.putUUID("match",u.matchId());e.putInt("count",u.acceptedRequests());e.putLong("cooldown",u.cooldownUntilTick());list.add(e);});tag.put("usage",list);}
    @Override public void load(CompoundTag tag){super.load(tag);ownerId=tag.hasUUID("owner")?tag.getUUID("owner"):null;matchId=tag.hasUUID("match")?tag.getUUID("match"):null;activeDropId=tag.hasUUID("activeDrop")?tag.getUUID("activeDrop"):null;ownerName=tag.getString("ownerName");beaconDimension=tag.getString("beaconDimension");beaconPos=tag.contains("beaconPos")?BlockPos.of(tag.getLong("beaconPos")):null;usage.clear();for(Tag raw:tag.getList("usage",Tag.TAG_COMPOUND)){CompoundTag e=(CompoundTag)raw;ResourceLocation id=ResourceLocation.tryParse(e.getString("id"));if(id!=null&&e.hasUUID("match"))usage.put(id,new RadioUsage(e.getUUID("match"),e.getInt("count"),e.getLong("cooldown")));}}
}
