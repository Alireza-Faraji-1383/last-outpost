package dev.exodus.map;
import dev.exodus.wasteland.arena.*;import net.minecraft.core.BlockPos;import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ArenaMapCatalogTest {
 @Test void catalogUsesGivenArenaInsteadOfLatest(){dev.exodus.ExodusConfig.SPEC.setConfig(com.electronwill.nightconfig.core.CommentedConfig.inMemory());var registry=new ArenaRegistry();var a=registry.begin(UUID.randomUUID(),0,0,0,2000,128);registry.ready(a.id());registry.consume(a.id());var b=registry.begin(UUID.randomUUID(),1,4096,0,2000,128);a.checkpoint().placementY.put("russian_base_part_0",64);var locations=ArenaMapCatalog.locations(a,new BlockPos(120,64,-160));assertEquals(120,locations.stream().filter(l->l.kind()==MapLocation.Kind.CITY).findFirst().orElseThrow().x());assertFalse(locations.stream().anyMatch(l->l.x()>=3000));}
}
