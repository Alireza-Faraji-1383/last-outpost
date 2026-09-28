package dev.exodus.supply.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import dev.exodus.ExodusConfig;
import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.supply.blockentity.SupplyCrateBlockEntity;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import dev.exodus.supply.domain.SupplyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import java.util.UUID;
import dev.exodus.ExodusSavedData;

public class SupplyDropEntity extends Entity {
    private UUID dropId,matchId,requester; private BlockPos radioPos,beaconPos; private String supplyId="",displayName="",lootTable="";private int smokeColor=0xFFFFFF;
    public SupplyDropEntity(EntityType<? extends SupplyDropEntity> type, Level level) {
        super(type, level);
        setGlowingTag(true);
    }
    @Override protected void defineSynchedData() {}
    public void configure(UUID drop,UUID match,BlockPos radio,BlockPos beacon,SupplyDefinition d,UUID requester){dropId=drop;matchId=match;radioPos=radio.immutable();beaconPos=beacon.immutable();supplyId=d.id();displayName=d.displayName();lootTable=d.lootTableId();smokeColor=d.smokeColor();this.requester=requester;}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel server)||dropId==null){discard();return;}if(matchId==null||!matchId.equals(ExodusSavedData.get(server.getServer()).matchId)){clearRadio(server);discard();return;}double speed=ExodusConfig.DROP_SPEED_MILLIBLOCKS.get()/1000.0;BlockPos next=BlockPos.containing(getX(),getY()-speed,getZ());server.sendParticles(new DustParticleOptions(new Vector3f(((smokeColor>>16)&255)/255f,((smokeColor>>8)&255)/255f,(smokeColor&255)/255f),1f),getX(),getY(),getZ(),2,.15,.05,.15,0);if(!server.getBlockState(next.below()).getCollisionShape(server,next.below()).isEmpty()&&server.getBlockState(next).canBeReplaced()){server.setBlock(next,ExodusSupplyRegistry.SUPPLY_CRATE.get().defaultBlockState(),3);if(server.getBlockEntity(next) instanceof SupplyCrateBlockEntity crate)crate.configure(displayName,new ResourceLocation(lootTable),requester);clearRadio(server);discard();return;}setPos(getX(),getY()-speed,getZ());if(getY()<server.getMinBuildHeight()){clearRadio(server);discard();}}
    private void clearRadio(ServerLevel server){if(radioPos!=null&&server.getBlockEntity(radioPos) instanceof SupplyRadioBlockEntity radio)radio.clearInFlight(dropId);}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {if(tag.hasUUID("drop"))dropId=tag.getUUID("drop");if(tag.hasUUID("match"))matchId=tag.getUUID("match");if(tag.hasUUID("requester"))requester=tag.getUUID("requester");if(tag.contains("radio"))radioPos=BlockPos.of(tag.getLong("radio"));if(tag.contains("beacon"))beaconPos=BlockPos.of(tag.getLong("beacon"));supplyId=tag.getString("supply");displayName=tag.getString("name");lootTable=tag.getString("loot");smokeColor=tag.getInt("smoke");}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {if(dropId!=null)tag.putUUID("drop",dropId);if(matchId!=null)tag.putUUID("match",matchId);if(requester!=null)tag.putUUID("requester",requester);if(radioPos!=null)tag.putLong("radio",radioPos.asLong());if(beaconPos!=null)tag.putLong("beacon",beaconPos.asLong());tag.putString("supply",supplyId);tag.putString("name",displayName);tag.putString("loot",lootTable);tag.putInt("smoke",smokeColor);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
