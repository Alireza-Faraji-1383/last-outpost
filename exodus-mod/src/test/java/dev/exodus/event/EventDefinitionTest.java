package dev.exodus.event;

import com.google.gson.*;
import dev.exodus.event.domain.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EventDefinitionTest {
    @Test void datapackObjectiveOverridesArePreservedAndValidateSelectors(){
        var json=JsonParser.parseString("""
                {"title":"Custom Hunt","scope":"GLOBAL","objective":"KILL_ENTITY","durationSeconds":45,
                 "targetEntity":"minecraft:zombie","targetCount":7,"rewardEmeralds":3,"participantSelector":"ALL_ACTIVE"}
                """).getAsJsonObject();
        var d=EventDefinitionParser.parse("exodus:custom",json);
        assertEquals(45,d.durationSeconds());assertEquals(7,d.targetCount());assertEquals(3,d.rewardEmeralds());
        json.addProperty("participantSelector","RANDOM_PAIR");assertThrows(IllegalArgumentException.class,()->EventDefinitionParser.parse("exodus:custom",json));
    }
    @Test void bundledCatalogHasExactlyOneGuaranteedCoreAndValidLootReferences()throws Exception{
        Path root=Path.of("src/main/resources/data/exodus");List<EventDefinition> definitions=new ArrayList<>();
        try(var files=Files.list(root.resolve("exodus_events"))){for(Path file:files.toList())definitions.add(EventDefinitionParser.parse("exodus:"+file.getFileName().toString().replace(".json",""),JsonParser.parseString(Files.readString(file)).getAsJsonObject()));}
        var core=definitions.stream().filter(EventDefinition::core).toList();assertEquals(1,core.size());assertEquals(5,core.get(0).milestoneDay());assertTrue(core.get(0).oneTime());
        for(var d:definitions)if(d.objective()==EventDefinition.Objective.WORLD_DROP)assertTrue(Files.exists(root.resolve("loot_tables/"+d.lootTable().split(":")[1]+".json")));
    }
    @Test void eventLootNeverRollsTheUniqueCoreOrDuplicatesMissingNestedTables()throws Exception{
        Path root=Path.of("src/main/resources/data/exodus/loot_tables");
        try(var files=Files.list(root.resolve("events"))){for(Path file:files.toList())check(JsonParser.parseString(Files.readString(file)),root);}
    }
    private void check(JsonElement element,Path root){
        if(element.isJsonArray()){for(var child:element.getAsJsonArray())check(child,root);}
        else if(element.isJsonObject()){
            var j=element.getAsJsonObject();if(j.has("name")){String name=j.get("name").getAsString();assertNotEquals("exodus:dimensional_core",name);
                if(j.has("type")&&j.get("type").getAsString().equals("minecraft:loot_table")&&name.startsWith("exodus:"))assertTrue(Files.exists(root.resolve(name.substring(7)+".json")),name);
            }for(var entry:j.entrySet())check(entry.getValue(),root);
        }
    }
}
