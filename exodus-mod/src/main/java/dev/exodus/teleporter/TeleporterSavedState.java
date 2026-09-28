package dev.exodus.teleporter;

import dev.exodus.teleporter.domain.TeleporterComponent;
import net.minecraft.nbt.*;
import java.util.*;

public final class TeleporterSavedState {
    public record Active(UUID matchId,String dimension,long blockPos,long startTick,int durationSeconds,int radiusMilliblocks,int capacity) {}
    private Active active;
    private final EnumSet<TeleporterComponent> rareClaims=EnumSet.noneOf(TeleporterComponent.class);
    private final Set<UUID> invalidatedMatches=new HashSet<>();
    public Active active(){return active;}
    public void active(Active value){active=value;}
    public Set<TeleporterComponent> rareClaims(){return rareClaims;}
    public Set<UUID> invalidatedMatches(){return invalidatedMatches;}
    public void clearCurrent(UUID matchId){if(matchId!=null)invalidatedMatches.add(matchId);active=null;rareClaims.clear();}

    public CompoundTag save(){
        CompoundTag tag=new CompoundTag();
        if(active!=null){CompoundTag a=new CompoundTag();a.putUUID("matchId",active.matchId());a.putString("dimension",active.dimension());a.putLong("blockPos",active.blockPos());a.putLong("startTick",active.startTick());a.putInt("durationSeconds",active.durationSeconds());a.putInt("radiusMilliblocks",active.radiusMilliblocks());a.putInt("capacity",active.capacity());tag.put("active",a);}
        ListTag claims=new ListTag();rareClaims.forEach(c->claims.add(StringTag.valueOf(c.name())));tag.put("rareClaims",claims);
        ListTag invalid=new ListTag();invalidatedMatches.forEach(id->{CompoundTag value=new CompoundTag();value.putUUID("id",id);invalid.add(value);});tag.put("invalidatedMatches",invalid);
        return tag;
    }
    public static TeleporterSavedState load(CompoundTag tag){
        TeleporterSavedState state=new TeleporterSavedState();
        if(tag.contains("active",Tag.TAG_COMPOUND)){CompoundTag a=tag.getCompound("active");if(a.hasUUID("matchId"))state.active=new Active(a.getUUID("matchId"),a.getString("dimension"),a.getLong("blockPos"),a.getLong("startTick"),a.getInt("durationSeconds"),a.getInt("radiusMilliblocks"),a.getInt("capacity"));}
        for(Tag raw:tag.getList("rareClaims",Tag.TAG_STRING))try{TeleporterComponent c=TeleporterComponent.valueOf(raw.getAsString());if(c.rare())state.rareClaims.add(c);}catch(IllegalArgumentException ignored){}
        for(Tag raw:tag.getList("invalidatedMatches",Tag.TAG_COMPOUND)){CompoundTag value=(CompoundTag)raw;if(value.hasUUID("id"))state.invalidatedMatches.add(value.getUUID("id"));}
        return state;
    }
}
