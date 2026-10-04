package dev.exodus.wasteland.loot;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpandedEquipmentLootTest {
    private static final Path ROOT=Path.of("src/main/resources/data/exodus/loot_tables/chests");
    private JsonObject read(String name)throws Exception {
        Path path=ROOT.resolve(name+".json");
        assertTrue(Files.exists(path),"Missing equipment table: "+name);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
    @Test void everyChestFamilyCanSelectBothExpandedPools()throws Exception {
        read("bonus/attachments");read("bonus/zero_contact");
        for(String name:List.of("general/common","general/standard","general/valuable","general/elite","food","medical","utility","tech","weapons")) {
            String table=read(name).toString();
            assertTrue(table.contains("exodus:chests/bonus/attachments"),name);
            assertTrue(table.contains("exodus:chests/bonus/zero_contact"),name);
        }
    }
    @Test void allCatalogItemsHavePositiveWeightsAndCorrectIdentity()throws Exception {
        var catalog=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/exodus/equipment_catalog.json"))).getAsJsonObject();
        for(String group:List.of("attachments","zero_contact")) {
            var entries=read("bonus/"+group).getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries");
            assertEquals(catalog.getAsJsonArray(group).size(),entries.size());
            Set<String> identities=new HashSet<>();
            for(var raw:entries) {
                var entry=raw.getAsJsonObject();assertTrue(entry.get("weight").getAsInt()>0);
                assertTrue(identities.add(entry.toString()),"Duplicate item");
                if(group.equals("attachments")) {
                    assertEquals("tacz:attachment",entry.get("name").getAsString());
                    assertTrue(entry.getAsJsonArray("functions").get(0).toString().contains("AttachmentId"));
                } else assertTrue(entry.get("name").getAsString().startsWith("zerocontact:"));
            }
        }
        // Compare all pairs: a greater maximum zoom must never be more common.
        for(var a:catalog.getAsJsonArray("attachments"))for(var b:catalog.getAsJsonArray("attachments")) {
            var x=a.getAsJsonObject();var y=b.getAsJsonObject();
            if(x.get("zoom").getAsDouble()>0&&y.get("zoom").getAsDouble()>x.get("zoom").getAsDouble())
                assertTrue(y.get("weight").getAsInt()<=x.get("weight").getAsInt());
        }
    }
}
