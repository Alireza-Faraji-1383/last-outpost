package dev.exodus.supply.catalog;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BalanceCatalogTest {
    private JsonObject json(String path) throws Exception {
        try(var in=getClass().getResourceAsStream("/data/exodus/"+path+".json")) {
            assertNotNull(in,path);
            return JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test void basicFoodIsGenerousAtTheSamePrice() throws Exception {
        var d=json("exodus_supply_drops/basic_food");
        assertEquals(4,d.getAsJsonObject("cost").get("count").getAsInt());
        String loot=json("loot_tables/supply_drops/basic_food").toString();
        assertTrue(loot.contains("24")); assertTrue(loot.contains("16")); assertTrue(loot.contains("8"));
    }
    @Test void variantsShareQuotaAndExplainContents() throws Exception {
        var d=SupplyJsonParser.parse("exodus:basic_light_9mm",json("exodus_supply_drops/basic_light_9mm")).definition();
        assertEquals("exodus:basic_light",d.quotaGroup());
        assertTrue(d.contents().contains("96"));
        assertEquals(3,d.maxRequests()); assertEquals(30,d.cooldownSeconds());
    }
    @Test void specialSniperHasScopeAndExactAmmo() throws Exception {
        var d=json("exodus_supply_drops/special_sniper_ai_awp");
        assertEquals(36,d.getAsJsonObject("cost").get("count").getAsInt());
        assertEquals(2,d.get("max_requests").getAsInt());
        assertEquals(90,d.get("cooldown_seconds").getAsInt());
        var loot=json("loot_tables/supply_drops/special_sniper_ai_awp").toString();
        assertTrue(loot.contains("AttachmentSCOPE")); assertTrue(loot.contains("AmmoId"));
    }
    @Test void allTwelveCategoriesKeepApprovedPriceQuotaAndCooldown()throws Exception{
        Map<String,Integer> prices=Map.ofEntries(Map.entry("basic_food",4),Map.entry("basic_light",5),Map.entry("basic_combat",7),Map.entry("basic_building",4),Map.entry("basic_industrial",8),Map.entry("basic_armor",8),Map.entry("special_industrial",24),Map.entry("special_rifle",30),Map.entry("special_sniper",36),Map.entry("special_armor",40),Map.entry("special_minigun",50),Map.entry("special_rpg",45));
        Set<String> found=new HashSet<>();
        try(var files=Files.list(Path.of("src/main/resources/data/exodus/exodus_supply_drops"))){
            for(Path file:files.toList()){
                String id=file.getFileName().toString().replace(".json","");
                var parsed=SupplyJsonParser.parse("exodus:"+id,JsonParser.parseString(Files.readString(file)).getAsJsonObject());
                assertTrue(parsed.errors().isEmpty(),id+parsed.errors());
                var d=parsed.definition();String group=d.quotaGroup().replace("exodus:","");
                if(!prices.containsKey(group))continue;
                found.add(group);boolean special=group.startsWith("special_");
                assertEquals(prices.get(group).intValue(),d.cost().count());
                assertEquals(special?2:3,d.maxRequests());assertEquals(special?90:30,d.cooldownSeconds());
                assertFalse(d.contents().isBlank());
                assertTrue(Files.isRegularFile(Path.of("src/main/resources/data/exodus/loot_tables",d.lootTableId().replace("exodus:","")+".json")));
            }
        }
        assertEquals(prices.keySet(),found);
    }
}
