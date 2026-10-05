package dev.exodus.event;

import com.google.gson.*;
import dev.exodus.event.domain.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EventDefinitionTest {
    @Test void shippedEventsHaveApprovedUnlockOrderAndIndependentChanceSettings()throws Exception{
        var expected=Map.of("airdrop_rpg",List.of(8,1,10,20),"airdrop_sniper",List.of(7,2,10,20),"airdrop_armor",List.of(6,3,10,20),
                "airdrop_combat_plus",List.of(5,4,10,20),"airdrop_medical_plus",List.of(4,5,10,20),"airdrop_combat",List.of(3,6,20,20),
                "airdrop_equipment",List.of(2,7,30,30),"airdrop_medical",List.of(2,8,30,30),"manhunt",List.of(3,1,30,30),"zombie_hunt",List.of(2,2,30,30));
        for(var entry:expected.entrySet()){
            var d=EventDefinitionParser.parse("exodus:"+entry.getKey(),JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/exodus/exodus_events/"+entry.getKey()+".json"))).getAsJsonObject());
            assertEquals(entry.getValue(),List.of(d.minDay(),d.priority(),d.chancePercent(),d.chanceIncreasePercent()),entry.getKey());
            if(entry.getKey().equals("zombie_hunt")){assertEquals(EventDefinition.Scope.TARGETED,d.scope());assertEquals(EventDefinition.ParticipantSelector.SINGLE_ACTIVE,d.participantSelector());}
        }
    }
    @Test void upgradedLootIncludesGuaranteedPowerfulArmorAndMedicalUpgrade()throws Exception{
        var armor=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/exodus/loot_tables/events/armor.json"))).getAsJsonObject();
        assertEquals(4,armor.getAsJsonArray("pools").size());
        for(var pool:armor.getAsJsonArray("pools")){
            var item=pool.getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject();assertTrue(item.get("name").getAsString().startsWith("lrarmor:defender_"));
            var enchants=item.getAsJsonArray("functions").asList().stream().map(JsonElement::getAsJsonObject).filter(f->f.get("function").getAsString().equals("minecraft:set_enchantments")).findFirst().orElseThrow().getAsJsonObject("enchantments");
            assertEquals(4,enchants.get("minecraft:protection").getAsInt());assertEquals(3,enchants.get("minecraft:unbreaking").getAsInt());
        }
        assertTrue(Files.readString(Path.of("src/main/resources/data/exodus/loot_tables/events/medical_plus.json")).contains("minecraft:enchanted_golden_apple"));
    }
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
