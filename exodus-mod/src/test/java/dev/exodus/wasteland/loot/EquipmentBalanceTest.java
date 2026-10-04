package dev.exodus.wasteland.loot;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentBalanceTest {
    private static final Path DATA=Path.of("src/main/resources/data");
    @Test void allLrArmorPiecesMeetIronProtectionAndDurabilityFloor()throws Exception {
        Map<String,int[]> iron=Map.of("helmet",new int[]{2,165},"chestplate",new int[]{6,240},"leggings",new int[]{5,225},"boots",new int[]{2,195});
        try(var files=Files.list(DATA.resolve("lrarmor/armor_data"))) {
            var paths=files.toList();assertEquals(16,paths.size());
            for(Path path:paths) {
                var data=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                for(var slot:iron.entrySet()) {
                    var part=data.getAsJsonObject(slot.getKey());
                    assertTrue(part.get("defense").getAsInt()>=slot.getValue()[0],path+":"+slot.getKey());
                    assertTrue(part.get("maxDurability").getAsInt()>=slot.getValue()[1],path+":"+slot.getKey());
                }
            }
        }
    }
    @Test void chestProbabilitiesMatchBalanceAndPreserveOtherEquipmentChances()throws Exception {
        Map<String,double[]> expected=Map.of("general/common",new double[]{.35,.45,.22,.18},"general/standard",new double[]{.50,.60,.22,.28},"general/valuable",new double[]{.65,.75,.40,.40},"general/elite",new double[]{.80,.90,.40,.55},"food",new double[]{.50,.60,.22,.28},"medical",new double[]{.50,.60,.22,.28},"utility",new double[]{.50,.60,.22,.28},"tech",new double[]{.50,.60,.22,.28},"weapons",new double[]{.50,.60,.22,.28});
        List<String> kinds=List.of("armor","attachments","equipment","zero_contact");
        for(var chest:expected.entrySet()) {
            var data=JsonParser.parseString(Files.readString(DATA.resolve("exodus/loot_tables/chests/"+chest.getKey()+".json"))).getAsJsonObject();
            double[] actual=new double[4];
            for(var raw:data.getAsJsonArray("pools")) {
                var pool=raw.getAsJsonObject();double chance=1,total=0;
                if(pool.has("conditions"))for(var condition:pool.getAsJsonArray("conditions"))chance*=condition.getAsJsonObject().get("chance").getAsDouble();
                for(var entry:pool.getAsJsonArray("entries"))total+=entry.getAsJsonObject().has("weight")?entry.getAsJsonObject().get("weight").getAsDouble():1;
                for(var entry:pool.getAsJsonArray("entries")) {
                    var e=entry.getAsJsonObject();String id=e.get("name").getAsString();
                    for(int i=0;i<4;i++)if(id.equals("exodus:chests/bonus/"+kinds.get(i)))actual[i]+=chance*(e.has("weight")?e.get("weight").getAsDouble():1)/total;
                }
            }
            assertArrayEquals(chest.getValue(),actual,1e-6,chest.getKey());
        }
    }
}
