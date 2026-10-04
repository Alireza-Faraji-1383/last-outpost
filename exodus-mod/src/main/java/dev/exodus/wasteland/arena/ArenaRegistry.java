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
    public void cancel(UUID id){ArenaRecord r=find(id);if(r.state()!=ArenaState.PREPARING&&r.state()!=ArenaState.READY)throw new IllegalStateException("Arena is not preparing or ready");r.transition(ArenaState.ABANDONED,"");}
    public void fail(UUID id,String reason){preparing(id).transition(ArenaState.FAILED,Objects.requireNonNull(reason));}
    private ArenaRecord preparing(UUID id){ArenaRecord r=find(id);if(r.state()!=ArenaState.PREPARING)throw new IllegalStateException("Arena is not preparing");return r;}
    private ArenaRecord find(UUID id){return records.stream().filter(r->r.id().equals(id)).findFirst().orElseThrow();}
    public ListTag save(){
        ListTag list=new ListTag();
        for(ArenaRecord r:records){
            CompoundTag t=new CompoundTag();t.putUUID("id",r.id());t.putUUID("initiator",r.initiator());t.putLong("ordinal",r.ordinal());t.putLong("centerX",r.centerX());t.putLong("centerZ",r.centerZ());t.putInt("arenaSize",r.arenaSize());t.putInt("buffer",r.buffer());t.putString("state",r.state().name());t.putString("failure",r.failure());
            PreparationCheckpoint c=r.checkpoint();t.putString("phase",c.phase.name());t.putInt("layoutVersion",c.layoutVersion);t.putInt("chunkCursor",c.chunkCursor);t.putInt("citySamples",c.citySamples);t.putInt("cityChunks",c.cityChunks);t.putInt("lastAnnouncedPercent",c.lastAnnouncedPercent);t.putBoolean("placementNeedsRevalidation",c.placementNeedsRevalidation);
            ListTag done=new ListTag();c.completedPlacements.forEach(v->done.add(StringTag.valueOf(v)));t.put("completedPlacements",done);
            CompoundTag elevations=new CompoundTag();c.placementY.forEach(elevations::putInt);t.put("placementY",elevations);
            CompoundTag poiStates=new CompoundTag();
            c.poiStates.forEach((id,value)->{CompoundTag saved=new CompoundTag();saved.putInt("chunkCursor",value.chunkCursor);saved.putInt("totalChunks",value.totalChunks);saved.putString("geometrySignature",value.geometrySignature);saved.putBoolean("chunksComplete",value.chunksComplete);saved.putBoolean("surfaceSelected",value.surfaceSelected);saved.putInt("platformY",value.platformY);saved.putInt("quorumCount",value.quorumCount);saved.putInt("totalColumns",value.totalColumns);saved.putInt("validSamples",value.validSamples);saved.putInt("unsupportedSamples",value.unsupportedSamples);saved.putInt("minimumSupportY",value.minimumSupportY);saved.putInt("maximumSupportY",value.maximumSupportY);saved.putInt("clearedBlocks",value.clearedBlocks);saved.putInt("foundationBlocks",value.foundationBlocks);saved.putInt("deepestFill",value.deepestFill);saved.putBoolean("terrainPrepared",value.terrainPrepared);saved.putBoolean("structurePlaced",value.structurePlaced);saved.putBoolean("verified",value.verified);poiStates.put(id,saved);});
            t.put("poiStates",poiStates);list.add(t);
        }
        return list;
    }
    public static ArenaRegistry load(ListTag list){
        ArenaRegistry registry=new ArenaRegistry();
        for(Tag raw:list){
            CompoundTag t=(CompoundTag)raw;ArenaState state;String failure=t.getString("failure");
            try{state=ArenaState.valueOf(t.getString("state"));}catch(Exception e){state=ArenaState.FAILED;failure="Unknown persisted arena state";}
            ArenaRecord r=ArenaRecord.restore(t.getUUID("id"),t.getUUID("initiator"),t.getLong("ordinal"),t.getLong("centerX"),t.getLong("centerZ"),t.getInt("arenaSize"),t.getInt("buffer"),state,failure);PreparationCheckpoint c=r.checkpoint();
            try{c.phase=ArenaPhase.valueOf(t.getString("phase"));}catch(Exception e){c.phase=ArenaPhase.TEMPLATE_VALIDATION;c.placementNeedsRevalidation=true;}
            c.layoutVersion=t.getInt("layoutVersion");c.chunkCursor=t.getInt("chunkCursor");c.citySamples=t.getInt("citySamples");c.cityChunks=t.getInt("cityChunks");c.lastAnnouncedPercent=t.getInt("lastAnnouncedPercent");c.placementNeedsRevalidation|=t.getBoolean("placementNeedsRevalidation");
            for(Tag value:t.getList("completedPlacements",Tag.TAG_STRING))c.completedPlacements.add(value.getAsString());
            CompoundTag elevations=t.getCompound("placementY");for(String key:elevations.getAllKeys())c.placementY.put(key,elevations.getInt(key));
            boolean hasPoiStates=t.contains("poiStates",Tag.TAG_COMPOUND)&&!t.getCompound("poiStates").getAllKeys().isEmpty();
            if(hasPoiStates){CompoundTag savedStates=t.getCompound("poiStates");for(String id:savedStates.getAllKeys()){CompoundTag saved=savedStates.getCompound(id);PoiPreparationState value=new PoiPreparationState();value.chunkCursor=saved.getInt("chunkCursor");value.totalChunks=saved.getInt("totalChunks");value.geometrySignature=saved.getString("geometrySignature");value.chunksComplete=saved.getBoolean("chunksComplete");value.surfaceSelected=saved.getBoolean("surfaceSelected");value.platformY=saved.getInt("platformY");value.quorumCount=saved.getInt("quorumCount");value.totalColumns=saved.getInt("totalColumns");value.validSamples=saved.getInt("validSamples");value.unsupportedSamples=saved.getInt("unsupportedSamples");value.minimumSupportY=saved.getInt("minimumSupportY");value.maximumSupportY=saved.getInt("maximumSupportY");value.clearedBlocks=saved.getInt("clearedBlocks");value.foundationBlocks=saved.getInt("foundationBlocks");value.deepestFill=saved.getInt("deepestFill");value.terrainPrepared=saved.getBoolean("terrainPrepared");value.structurePlaced=saved.getBoolean("structurePlaced");value.verified=saved.getBoolean("verified");c.poiStates.put(id,value);}}
            if(!hasPoiStates&&state==ArenaState.PREPARING&&requiresPoiMigration(c.phase)){r.transition(ArenaState.FAILED,"Cannot safely resume legacy arena after terrain mutation");}
            if(hasPoiStates&&state==ArenaState.PREPARING){for(PoiPreparationState value:c.poiStates.values()){if(value.verified)value.verified=false;if(value.terrainPrepared&&!value.structurePlaced)value.terrainPrepared=false;}c.placementNeedsRevalidation=false;}
            registry.records.add(r);
        }
        return registry;
    }
    private static boolean requiresPoiMigration(ArenaPhase phase){return phase==ArenaPhase.TERRAIN_PREPARATION||phase==ArenaPhase.STRUCTURE_PLACEMENT||phase==ArenaPhase.MARKER_PROCESSING||phase==ArenaPhase.POI_VERIFICATION||phase==ArenaPhase.FINAL_VALIDATION;}
}
