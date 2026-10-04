package dev.exodus.party;
import dev.exodus.device.ComponentProgressPolicy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class PartyProgressTest {
 @Test void duplicatesCountOnlyOnce(){assertEquals(1,ComponentProgressPolicy.partyCount(List.of(1,1),List.of()));}
 @Test void differentPiecesUnionAndDisappearingPiecesReduceCount(){assertEquals(2,ComponentProgressPolicy.partyCount(List.of(1,2),List.of()));assertEquals(1,ComponentProgressPolicy.partyCount(List.of(0,2),List.of()));}
 @Test void devicesAreEvaluatedSeparatelyAgainstPooledInventories(){assertEquals(3,ComponentProgressPolicy.partyCount(List.of(1,2),List.of(4,8)));assertEquals(2,ComponentProgressPolicy.partyCount(List.of(0,0),List.of(3,12)));}
 @Test void onlyNineBitsCountAndSoloBehaviorIsPreserved(){assertEquals(9,ComponentProgressPolicy.partyCount(List.of(1023,0),List.of()));assertEquals(ComponentProgressPolicy.bestCount(3,List.of(4,8)),ComponentProgressPolicy.partyCount(List.of(3),List.of(4,8)));}
}
