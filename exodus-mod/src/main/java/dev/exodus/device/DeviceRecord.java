package dev.exodus.device;
import java.util.UUID;
public record DeviceRecord(UUID matchId,String dimension,long position,UUID owner,UUID protectedOwner,int componentMask,boolean active) {
 public DeviceRecord {if(matchId==null||dimension==null||dimension.isBlank())throw new IllegalArgumentException("Missing device identity");componentMask&=511;}
 public DeviceRecord ownedBy(UUID player){return new DeviceRecord(matchId,dimension,position,player,protectedOwner,componentMask,active);}
 public DeviceRecord contents(int mask,boolean locked){return new DeviceRecord(matchId,dimension,position,owner,protectedOwner,mask,locked);}
}
