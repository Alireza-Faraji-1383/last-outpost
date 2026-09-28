package dev.exodus.wasteland.arena;

import java.util.*;
import net.minecraft.nbt.*;

public final class ArenaRegistry {
    private final List<ArenaRecord> records=new ArrayList<>();
    public ArenaRecord begin(UUID initiator,long ordinal,long centerX,long centerZ,int arenaSize,int buffer){
        if(active().isPresent()||readyArena().isPresent())throw new IllegalStateException("An arena is already active or ready");
        ArenaRecord record=new ArenaRecord(UUID.randomUUID(),initiator,ordinal,centerX,centerZ,arenaSize,buffer);records.add(record);return record;
    }
    public Optional<ArenaRecord> active(){return records.stream().filter(r->r.state()==ArenaState.PREPARING).findFirst();}
    public Optional<ArenaRecord> readyArena(){return records.stream().filter(r->r.state()==ArenaState.READY).findFirst();}
    public List<ArenaRecord> records(){return List.copyOf(records);}
    public void ready(UUID id){ArenaRecord r=preparing(id);r.checkpoint().phase=ArenaPhase.COMPLETE;r.transition(ArenaState.READY,"");}
    public void consume(UUID id){ArenaRecord r=find(id);if(r.state()!=ArenaState.READY)throw new IllegalStateException("Arena is not ready");r.transition(ArenaState.CONSUMED,"");}
    public void cancel(UUID id){preparing(id).transition(ArenaState.ABANDONED,"");}
    public void fail(UUID id,String reason){preparing(id).transition(ArenaState.FAILED,Objects.requireNonNull(reason));}
    private ArenaRecord preparing(UUID id){ArenaRecord r=find(id);if(r.state()!=ArenaState.PREPARING)throw new IllegalStateException("Arena is not preparing");return r;}
    private ArenaRecord find(UUID id){return records.stream().filter(r->r.id().equals(id)).findFirst().orElseThrow();}
    public ListTag save(){ListTag list=new ListTag();for(ArenaRecord r:records){CompoundTag t=new CompoundTag();t.putUUID("id",r.id());t.putUUID("initiator",r.initiator());t.putLong("ordinal",r.ordinal());t.putLong("centerX",r.centerX());t.putLong("centerZ",r.centerZ());t.putInt("arenaSize",r.arenaSize());t.putInt("buffer",r.buffer());t.putString("state",r.state().name());t.putString("failure",r.failure());PreparationCheckpoint c=r.checkpoint();t.putString("phase",c.phase.name());t.putInt("chunkCursor",c.chunkCursor);t.putInt("citySamples",c.citySamples);t.putInt("cityChunks",c.cityChunks);t.putInt("lastAnnouncedPercent",c.lastAnnouncedPercent);t.putBoolean("placementNeedsRevalidation",c.placementNeedsRevalidation);ListTag done=new ListTag();c.completedPlacements.forEach(v->done.add(StringTag.valueOf(v)));t.put("completedPlacements",done);list.add(t);}return list;}
    public static ArenaRegistry load(ListTag list){ArenaRegistry registry=new ArenaRegistry();for(Tag raw:list){CompoundTag t=(CompoundTag)raw;ArenaState state;String failure=t.getString("failure");try{state=ArenaState.valueOf(t.getString("state"));}catch(Exception e){state=ArenaState.FAILED;failure="Unknown persisted arena state";}ArenaRecord r=ArenaRecord.restore(t.getUUID("id"),t.getUUID("initiator"),t.getLong("ordinal"),t.getLong("centerX"),t.getLong("centerZ"),t.getInt("arenaSize"),t.getInt("buffer"),state,failure);PreparationCheckpoint c=r.checkpoint();try{c.phase=ArenaPhase.valueOf(t.getString("phase"));}catch(Exception e){c.phase=ArenaPhase.TEMPLATE_VALIDATION;c.placementNeedsRevalidation=true;}c.chunkCursor=t.getInt("chunkCursor");c.citySamples=t.getInt("citySamples");c.cityChunks=t.getInt("cityChunks");c.lastAnnouncedPercent=t.getInt("lastAnnouncedPercent");c.placementNeedsRevalidation|=t.getBoolean("placementNeedsRevalidation");for(Tag value:t.getList("completedPlacements",Tag.TAG_STRING))c.completedPlacements.add(value.getAsString());registry.records.add(r);}return registry;}
}
