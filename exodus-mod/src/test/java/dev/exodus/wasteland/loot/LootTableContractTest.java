package dev.exodus.wasteland.loot;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LootTableContractTest {
    private static final Set<String> MARKERS=Set.of("general/common","general/standard","general/valuable","general/elite","food","weapons","medical","utility","tech");
    private static final Set<String> BONUSES=Set.of("bonus/survival","bonus/blocks","bonus/ammunition","bonus/weapons_common","bonus/weapons_valuable","bonus/weapons_elite","bonus/components_common","bonus/components_standard","bonus/components_valuable","bonus/components_elite");
    private static final Set<String> COMPONENTS=Set.of("exodus:reinforced_frame","exodus:power_regulator","exodus:phase_coil","exodus:signal_processor","exodus:spatial_lens","exodus:containment_module");

    @Test void everyOrdinaryTableParsesAndExcludesRareComponents()throws Exception{
        Set<String> tables=new HashSet<>(MARKERS);tables.addAll(BONUSES);
        for(String name:tables){JsonObject json=table(name);assertTrue(json.has("pools"),name);String encoded=json.toString();assertFalse(encoded.contains("facility_alpha_key"),name);assertFalse(encoded.contains("facility_beta_key"),name);assertFalse(encoded.contains("dimensional_core"),name);}
    }

    @Test void factionTablesKeepTheirExistingGuaranteedKeys()throws Exception{
        String russian=resource("/data/exodus/loot_tables/chests/faction/russian_key.json"),american=resource("/data/exodus/loot_tables/chests/faction/american_key.json");
        assertTrue(russian.contains("exodus:chests/general/elite"));assertTrue(russian.contains("exodus:facility_alpha_key"));assertFalse(russian.contains("exodus:facility_beta_key"));
        assertTrue(american.contains("exodus:chests/general/elite"));assertTrue(american.contains("exodus:facility_beta_key"));assertFalse(american.contains("exodus:facility_alpha_key"));
    }

    @Test void allSixCraftableComponentsScaleUpByTier()throws Exception{
        float previous=0;
        for(String tier:new String[]{"common","standard","valuable","elite"}){JsonObject pool=table("bonus/components_"+tier).getAsJsonArray("pools").get(0).getAsJsonObject();Set<String> names=new HashSet<>();pool.getAsJsonArray("entries").forEach(raw->names.add(raw.getAsJsonObject().get("name").getAsString()));assertEquals(COMPONENTS,names,tier);float chance=pool.getAsJsonArray("conditions").get(0).getAsJsonObject().get("chance").getAsFloat();assertTrue(chance>previous,tier);previous=chance;}
    }

    @Test void allMarkerFamiliesReferenceCraftableComponents()throws Exception{for(String name:MARKERS)assertTrue(table(name).toString().contains("bonus/components_"),name);}

    @Test void generalTiersReachTechAndFavorAmmunitionOverGuns()throws Exception{
        assertTrue(table("bonus/survival").toString().contains("exodus:chests/category/tech"));
        assertTrue(table("bonus/ammunition").toString().contains("tacz:ammo"));
        for(String tier:new String[]{"common","standard","valuable","elite"}){
            JsonArray pools=table("general/"+tier).getAsJsonArray("pools");
            float ammunitionChance=chanceForReference(pools,"exodus:chests/bonus/ammunition");
            float gunChance=chanceForReference(pools,"exodus:chests/bonus/weapons_");
            assertTrue(ammunitionChance>gunChance,tier+" ammunition="+ammunitionChance+" guns="+gunChance);
        }
    }

    @Test void weaponsIncludeMultiplePistolsSmgsRiflesShotgunsAndPrecisionRifles()throws Exception{
        String weapons=table("weapons")+table("bonus/weapons_common").toString()+table("bonus/weapons_valuable")+table("bonus/weapons_elite");
        for(String gun:new String[]{"glock_17","m1911","cz75","hk_mp5a5","uzi","ump45","ak47","m4a1","hk416d","scar_l","m870","m1014","ai_awp","m107","scar_h","mk14","minigun","rpg7"})weapons+=table("guns/"+gun);
        assertAtLeast(weapons,2,"tacz:glock_17","tacz:m1911","tacz:cz75");assertAtLeast(weapons,2,"tacz:hk_mp5a5","tacz:uzi","tacz:ump45");assertAtLeast(weapons,2,"tacz:ak47","tacz:m4a1","tacz:hk416d","tacz:scar_l");assertAtLeast(weapons,2,"tacz:m870","tacz:m1014");assertAtLeast(weapons,2,"tacz:ai_awp","tacz:m107","tacz:scar_h","tacz:mk14");assertTrue(weapons.contains("rpg7"));assertTrue(weapons.contains("minigun"));assertTrue(weightFor(weapons,"tacz:ammo")>weightFor(weapons,"tacz:modern_kinetic_gun"));
    }

    @Test void blockBonusContainsUsefulConstructionBlocks()throws Exception{String blocks=table("bonus/blocks").toString();for(String item:Set.of("minecraft:cobblestone","minecraft:stone_bricks","minecraft:oak_planks","minecraft:glass","minecraft:iron_bars","minecraft:ladder","minecraft:scaffolding","minecraft:torch"))assertTrue(blocks.contains(item),item);}

    @Test void markerRollBudgetsAreRoughlyDoubled()throws Exception{
        Map<String,Integer> minimum=Map.of("general/common",7,"general/standard",7,"general/valuable",5,"general/elite",7,"food",10,"medical",8,"weapons",8,"utility",8,"tech",7);
        for(var expected:minimum.entrySet()){JsonElement rolls=table(expected.getKey()).getAsJsonArray("pools").get(0).getAsJsonObject().get("rolls");int maximum=rolls.isJsonObject()?rolls.getAsJsonObject().get("max").getAsInt():rolls.getAsInt();assertTrue(maximum>=expected.getValue(),expected.getKey()+" max="+maximum);}
    }

    @Test void lostCitiesCanSelectEveryFamilyWithRareTiersOrdered()throws Exception{
        JsonArray values=JsonParser.parseString(resource("/data/lostcities/lostcities/conditions/chestloot.json")).getAsJsonObject().getAsJsonArray("values");Map<String,Integer> weights=new HashMap<>();values.forEach(raw->{JsonObject value=raw.getAsJsonObject();weights.put(value.get("value").getAsString(),value.get("factor").getAsInt());});
        for(String name:Set.of("exodus:chests/general/common","exodus:chests/general/standard","exodus:chests/general/valuable","exodus:chests/general/elite","exodus:chests/food","exodus:chests/medical","exodus:chests/weapons","exodus:chests/utility","exodus:chests/tech"))assertTrue(weights.containsKey(name),name);
        assertTrue(weights.get("exodus:chests/general/elite")<weights.get("exodus:chests/general/valuable"));assertTrue(weights.get("exodus:chests/general/valuable")<weights.get("exodus:chests/general/common"));assertEquals(100,weights.values().stream().mapToInt(Integer::intValue).sum());
    }

    private static JsonObject table(String name)throws Exception{return JsonParser.parseString(resource("/data/exodus/loot_tables/chests/"+name+".json")).getAsJsonObject();}
    private static String resource(String path)throws Exception{try(var stream=LootTableContractTest.class.getResourceAsStream(path)){assertNotNull(stream,path);return new String(stream.readAllBytes(),StandardCharsets.UTF_8);}}
    private static void assertAtLeast(String json,int minimum,String...ids){long found=Arrays.stream(ids).filter(json::contains).count();assertTrue(found>=minimum,"expected at least "+minimum+" of "+Arrays.toString(ids));}
    private static float chanceForReference(JsonArray pools,String referencePrefix){for(JsonElement rawPool:pools){JsonObject pool=rawPool.getAsJsonObject();for(JsonElement rawEntry:pool.getAsJsonArray("entries")){String name=rawEntry.getAsJsonObject().get("name").getAsString();if(name.startsWith(referencePrefix)){if(!pool.has("conditions"))return 1.0F;return pool.getAsJsonArray("conditions").get(0).getAsJsonObject().get("chance").getAsFloat();}}}fail("missing loot reference "+referencePrefix);return 0;}
    private static int weightFor(String json,String item){int total=0,cursor=0;while((cursor=json.indexOf("\"name\":\""+item+"\"",cursor))>=0){int at=json.indexOf("\"weight\":",cursor),start=at+9,end=start;while(Character.isDigit(json.charAt(end)))end++;total+=Integer.parseInt(json.substring(start,end));cursor=end;}return total;}
}
