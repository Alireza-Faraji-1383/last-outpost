package dev.exodus.wasteland.loot;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BalanceLootGraphTest {
    private static final Path ROOT=Path.of("src/main/resources/data/exodus/loot_tables");
    private JsonObject table(String name)throws Exception{return JsonParser.parseString(Files.readString(ROOT.resolve(name+".json"))).getAsJsonObject();}
    @Test void emeraldChanceCannotMultiplyThroughNestedCategoryLoot()throws Exception{
        for(String tier:List.of("common","standard","valuable","elite"))
            assertEquals(tier.equals("common")||tier.equals("standard")?.15:.25,1-noEmerald("chests/general/"+tier,new HashSet<>()),1e-6,tier);
        for(String category:List.of("food","medical","utility","tech","weapons"))
            assertEquals(.15,1-noEmerald("chests/"+category,new HashSet<>()),1e-6,category);
    }
    private double noEmerald(String name,Set<String> chain)throws Exception{
        assertTrue(chain.add(name),"Cyclic loot: "+name);
        double result=1;
        for(JsonElement raw:table(name).getAsJsonArray("pools")){
            JsonObject p=raw.getAsJsonObject();double total=0,none=0,chance=1;
            if(p.has("conditions"))for(JsonElement c:p.getAsJsonArray("conditions"))if(c.getAsJsonObject().get("condition").getAsString().equals("minecraft:random_chance"))chance*=c.getAsJsonObject().get("chance").getAsDouble();
            for(JsonElement rawEntry:p.getAsJsonArray("entries")){
                JsonObject e=rawEntry.getAsJsonObject();double w=e.has("weight")?e.get("weight").getAsDouble():1;total+=w;
                String id=e.get("name").getAsString();
                double probability=e.get("type").getAsString().equals("minecraft:loot_table")?noEmerald(id.substring("exodus:".length()),new HashSet<>(chain)):Set.of("minecraft:emerald","minecraft:emerald_block").contains(id)?0:1;
                none+=w*probability;
            }
            assertTrue(total>0,"Empty pool: "+name);
            JsonElement rolls=p.get("rolls");int min=rolls.isJsonObject()?rolls.getAsJsonObject().get("min").getAsInt():rolls.getAsInt();int max=rolls.isJsonObject()?rolls.getAsJsonObject().get("max").getAsInt():min;
            double noPool=0;for(int count=min;count<=max;count++)noPool+=Math.pow(none/total,count)/(max-min+1);
            result*=1-chance+chance*noPool;
        }
        return result;
    }
    @Test void ordinaryGunBundlesAlwaysHaveAmmoAndPrecisionScopes()throws Exception{
        try(var files=Files.walk(ROOT.resolve("chests/guns"))){
            for(Path file:files.filter(Files::isRegularFile).toList()){
                var json=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                assertEquals(2,json.getAsJsonArray("pools").size(),file.toString());
                String encoded=json.toString();assertTrue(encoded.contains("GunFireMode"));assertTrue(encoded.contains("AmmoId"));
                for(JsonElement p:json.getAsJsonArray("pools"))assertFalse(p.getAsJsonObject().has("conditions"));
                if(Set.of("ai_awp.json","m107.json","m95.json","mk14.json","ssg69.json").contains(file.getFileName().toString()))assertTrue(encoded.contains("AttachmentSCOPE"));
            }
        }
        String common=table("chests/bonus/weapons_common").toString();assertFalse(common.contains("minigun"));assertFalse(common.contains("ai_awp"));
        for(String tier:List.of("valuable","elite")){String high=table("chests/bonus/weapons_"+tier).toString();assertTrue(high.contains("minigun"));assertTrue(high.contains("ai_awp"));assertTrue(high.contains("rpg7"));}
    }
    @Test void maximumOutputsFitSingleChestWithoutDiscardingPairedAmmo()throws Exception{
        for(String name:List.of("general/common","general/standard","general/valuable","general/elite","food","medical","utility","tech","weapons")){
            int slots=maxSlots("chests/"+name);
            assertTrue(slots<=27,name+" needs "+slots+" slots");
        }
    }
    private int maxSlots(String name)throws Exception{
        int slots=0;
        for(JsonElement raw:table(name).getAsJsonArray("pools")){
            JsonObject p=raw.getAsJsonObject();int largest=0;
            for(JsonElement entry:p.getAsJsonArray("entries")){
                JsonObject e=entry.getAsJsonObject();String id=e.get("name").getAsString();int count=1;
                if(e.get("type").getAsString().equals("minecraft:loot_table"))count=maxSlots(id.replace("exodus:",""));
                else if(e.has("functions")){
                    for(JsonElement f:e.getAsJsonArray("functions"))if(f.getAsJsonObject().get("function").getAsString().equals("minecraft:set_count")){
                        JsonElement c=f.getAsJsonObject().get("count");count=c.isJsonObject()?c.getAsJsonObject().get("max").getAsInt():c.getAsInt();
                        int stack=id.equals("minecraft:ender_pearl")?16:64;count=(count+stack-1)/stack;
                    }
                }
                largest=Math.max(largest,count);
            }
            JsonElement rolls=p.get("rolls");slots+=largest*(rolls.isJsonObject()?rolls.getAsJsonObject().get("max").getAsInt():rolls.getAsInt());
        }
        return slots;
    }
}
