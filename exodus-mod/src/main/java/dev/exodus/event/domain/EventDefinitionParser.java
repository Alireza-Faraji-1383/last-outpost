package dev.exodus.event.domain;

import com.google.gson.JsonObject;

public final class EventDefinitionParser {
    private EventDefinitionParser(){}
    public static EventDefinition parse(String id,JsonObject j){
        var scope=EventDefinition.Scope.valueOf(j.get("scope").getAsString());
        return new EventDefinition(id,j.get("title").getAsString(),scope,EventDefinition.Objective.valueOf(j.get("objective").getAsString()),
                integer(j,"minDay",1),integer(j,"maxDay",0),integer(j,"weight",0),bool(j,"oneTime"),
                integer(j,"cooldownDays",1),integer(j,"milestoneDay",0),j.has("lootTable")?j.get("lootTable").getAsString():"",bool(j,"core"),
                integer(j,"durationSeconds",-1),integer(j,"targetCount",-1),j.has("targetEntity")?j.get("targetEntity").getAsString():"minecraft:zombie",
                integer(j,"rewardEmeralds",-1),integer(j,"hunterEmeralds",-1),integer(j,"preyEmeralds",-1),
                EventDefinition.ParticipantSelector.valueOf(j.has("participantSelector")?j.get("participantSelector").getAsString():scope==EventDefinition.Scope.TARGETED?"RANDOM_PAIR":"ALL_ACTIVE"));
    }
    private static int integer(JsonObject j,String key,int fallback){return j.has(key)?j.get(key).getAsInt():fallback;}
    private static boolean bool(JsonObject j,String key){return j.has(key)&&j.get(key).getAsBoolean();}
}
