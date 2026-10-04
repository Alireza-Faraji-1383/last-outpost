package dev.exodus.event;

import dev.exodus.event.domain.EventSchedule;
import dev.exodus.event.domain.ObjectiveProgress;
import net.minecraft.nbt.*;
import java.util.*;

/** Match history and active objectives survive restart. */
public final class EventSavedState {
    public final List<EventManager.Run> runs=new ArrayList<>();
    public final Set<String> pendingDrops=new LinkedHashSet<>();
    public record Drop(UUID id,UUID entity,long position,long expires,String title,boolean core,boolean landed){}
    public record Terminal(UUID id,String definition,ObjectiveProgress.Outcome outcome,long tick){}
    public final EventSchedule schedule=new EventSchedule();
    public final Map<UUID,Drop> drops=new LinkedHashMap<>();
    public final Set<String> paidRewards=new HashSet<>();
    public final Map<UUID,Terminal> outcomes=new LinkedHashMap<>();
    public CompoundTag save(){
        var tag=new CompoundTag();tag.putInt("lastDay",schedule.lastRolledDay());
        var active=new ListTag();for(var run:runs){var t=new CompoundTag();t.putUUID("id",run.id());t.putString("definition",new com.google.gson.Gson().toJson(run.definition()));t.put("progress",run.progress().save());t.putInt("huntReward",run.huntReward());t.putInt("hunterReward",run.hunterReward());t.putInt("preyReward",run.preyReward());active.add(t);}tag.put("runs",active);
        var pending=new ListTag();pendingDrops.forEach(id->pending.add(StringTag.valueOf(id)));tag.put("pendingDrops",pending);
        var history=new ListTag();schedule.history().forEach((id,day)->{var t=new CompoundTag();t.putString("id",id);t.putInt("day",day);history.add(t);});tag.put("history",history);
        var list=new ListTag();for(var d:drops.values()){var t=new CompoundTag();t.putUUID("id",d.id());t.putUUID("entity",d.entity());t.putLong("pos",d.position());t.putLong("expires",d.expires());t.putString("title",d.title());t.putBoolean("core",d.core());t.putBoolean("landed",d.landed());list.add(t);}tag.put("drops",list);
        var paid=new ListTag();paidRewards.forEach(id->paid.add(StringTag.valueOf(id)));tag.put("paid",paid);
        var outcomesTag=new ListTag();for(var result:outcomes.values()){var t=new CompoundTag();t.putUUID("id",result.id());t.putString("definition",result.definition());t.putString("outcome",result.outcome().name());t.putLong("tick",result.tick());outcomesTag.add(t);}tag.put("outcomes",outcomesTag);return tag;
    }
    public static EventSavedState load(CompoundTag tag){
        var state=new EventSavedState();Map<String,Integer> history=new HashMap<>();
        for(Tag raw:tag.getList("history",Tag.TAG_COMPOUND)){var t=(CompoundTag)raw;history.put(t.getString("id"),Math.max(1,t.getInt("day")));}state.schedule.restore(tag.getInt("lastDay"),history);
        for(Tag raw:tag.getList("drops",Tag.TAG_COMPOUND)){var t=(CompoundTag)raw;if(t.hasUUID("id")&&t.hasUUID("entity")){var d=new Drop(t.getUUID("id"),t.getUUID("entity"),t.getLong("pos"),t.getLong("expires"),t.getString("title"),t.getBoolean("core"),t.getBoolean("landed"));state.drops.put(d.id(),d);}}
        for(Tag raw:tag.getList("paid",Tag.TAG_STRING))state.paidRewards.add(raw.getAsString());
        for(Tag raw:tag.getList("outcomes",Tag.TAG_COMPOUND)){var t=(CompoundTag)raw;try{if(t.hasUUID("id")){var result=new Terminal(t.getUUID("id"),t.getString("definition"),ObjectiveProgress.Outcome.valueOf(t.getString("outcome")),t.getLong("tick"));state.outcomes.put(result.id(),result);}}catch(IllegalArgumentException ignored){}}
        for(Tag raw:tag.getList("runs",Tag.TAG_COMPOUND)){
            var t=(CompoundTag)raw;var json=com.google.gson.JsonParser.parseString(t.getString("definition")).getAsJsonObject();
            var def=dev.exodus.event.domain.EventDefinitionParser.parse(json.get("id").getAsString(),json);
            state.runs.add(new EventManager.Run(t.getUUID("id"),def,ObjectiveProgress.load(t.getCompound("progress")),t.getInt("huntReward"),t.getInt("hunterReward"),t.getInt("preyReward")));
        }
        for(Tag raw:tag.getList("pendingDrops",Tag.TAG_STRING))state.pendingDrops.add(raw.getAsString());
        return state;
    }
}
