package dev.exodus.device;
import dev.exodus.*;import dev.exodus.session.MatchSessionState;import dev.exodus.teleporter.blockentity.ExodusTeleporterBlockEntity;import dev.exodus.teleporter.item.*;import net.minecraft.core.*;import net.minecraft.server.level.*;import java.util.*;
/** Persisted installed-component index. Never loads a chunk to count components. */
public final class DeviceIndex {
 private DeviceIndex(){}
 public static void observe(ExodusTeleporterBlockEntity be){
  if(!(be.getLevel() instanceof ServerLevel level))return;
  ExodusSavedData d=ExodusSavedData.get(level.getServer());if(be.matchId()!=null&&d.teleporter.invalidatedMatches().contains(be.matchId()))be.reset();if(d.state!=MatchState.RUNNING||d.matchId==null||!level.dimension().location().toString().equals(d.dimension)||!level.getWorldBorder().isWithinBounds(be.getBlockPos()))return;
  if(!d.matchId.equals(be.matchId())){be.reset();be.matchId(d.matchId);}
  dev.exodus.player.RespawnService.reserveHeadroom(level,be.getBlockPos().above());
  long key=be.getBlockPos().asLong();DeviceRecord old=d.session.devices.get(key);
  int mask=0;for(int i=0;i<9;i++)if(ComponentStacks.isCurrent(be.getItem(i),d.matchId)&&be.getItem(i).getItem() instanceof TeleporterComponentItem item)mask|=1<<item.component().ordinal();
  DeviceRecord next=old==null?new DeviceRecord(d.matchId,d.dimension,key,null,null,mask,be.locked()):old.contents(mask,be.locked());
  if(!next.equals(old)){d.session.devices.put(key,next);d.setDirty();}
 }
 public static void scan(ServerLevel level,BlockPos origin,Vec3i size,UUID protectedOwner){
  ExodusSavedData data=ExodusSavedData.get(level.getServer());
  for(int x=origin.getX()>>4;x<=(origin.getX()+size.getX()-1)>>4;x++)for(int z=origin.getZ()>>4;z<=(origin.getZ()+size.getZ()-1)>>4;z++){
   var chunk=level.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;
   for(var be:List.copyOf(chunk.getBlockEntities().values()))if(be instanceof ExodusTeleporterBlockEntity device){BlockPos p=be.getBlockPos();if(p.getX()<origin.getX()||p.getX()>=origin.getX()+size.getX()||p.getY()<origin.getY()||p.getY()>=origin.getY()+size.getY()||p.getZ()<origin.getZ()||p.getZ()>=origin.getZ()+size.getZ())continue;
    observe(device);DeviceRecord record=data.session.devices.get(p.asLong());if(record==null)continue;
    if(protectedOwner!=null){record=new DeviceRecord(data.matchId,data.dimension,p.asLong(),protectedOwner,protectedOwner,record.componentMask(),record.active());data.session.devices.put(p.asLong(),record);Long original=data.session.originalSpawns.get(protectedOwner);if(original==null||p.distSqr(data.bases.get(protectedOwner).center())<BlockPos.of(original).distSqr(data.bases.get(protectedOwner).center()))data.session.originalSpawns.put(protectedOwner,p.asLong());}
   }
  }data.setDirty();
 }
 public static List<Integer> ownedMasks(MatchSessionState s,UUID owner){return s.devices.values().stream().filter(r->owner.equals(r.owner())&&Objects.equals(s.matchId,r.matchId())).map(DeviceRecord::componentMask).toList();}
}
