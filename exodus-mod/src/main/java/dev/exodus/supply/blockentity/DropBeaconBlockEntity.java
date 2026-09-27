package dev.exodus.supply.blockentity;

import dev.exodus.supply.ExodusSupplyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class DropBeaconBlockEntity extends BlockEntity {
    private UUID ownerId, matchId;
    private String ownerName = "", radioDimension = "";
    private BlockPos radioPos;

    public DropBeaconBlockEntity(BlockPos pos, BlockState state) { super(ExodusSupplyRegistry.DROP_BEACON_ENTITY.get(), pos, state); }
    public void claim(UUID match, UUID owner, String name, String dimension, BlockPos radio) {
        matchId=match; ownerId=owner; ownerName=name; radioDimension=dimension; radioPos=radio.immutable(); setChanged();
    }
    public UUID ownerId(){return ownerId;} public UUID matchId(){return matchId;} public BlockPos radioPos(){return radioPos;} public String radioDimension(){return radioDimension;}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);if(ownerId!=null)tag.putUUID("owner",ownerId);if(matchId!=null)tag.putUUID("match",matchId);tag.putString("ownerName",ownerName);tag.putString("radioDimension",radioDimension);if(radioPos!=null)tag.putLong("radioPos",radioPos.asLong());}
    @Override public void load(CompoundTag tag){super.load(tag);ownerId=tag.hasUUID("owner")?tag.getUUID("owner"):null;matchId=tag.hasUUID("match")?tag.getUUID("match"):null;ownerName=tag.getString("ownerName");radioDimension=tag.getString("radioDimension");radioPos=tag.contains("radioPos")?BlockPos.of(tag.getLong("radioPos")):null;}
}
