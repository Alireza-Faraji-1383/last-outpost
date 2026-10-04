package dev.exodus.map;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class PartyMapPrivacyTest {
 @Test void tagsRevealOnlyTeammateAndResetOnClear(){var friend=UUID.randomUUID();var stranger=UUID.randomUUID();var s=new MatchNameTagState();s.receive(new MatchMapSnapshot(1,1,UUID.randomUUID(),"exodus:wasteland",0,0,2000,false,List.of(),Set.of(friend)));assertFalse(s.hidden("exodus:wasteland",true,false,friend));assertTrue(s.hidden("exodus:wasteland",true,false,stranger));assertFalse(s.teammate("minecraft:overworld",friend));s.receive(new MatchMapSnapshot(2,1,null,"",0,0,0,true,List.of()));assertFalse(s.teammate("exodus:wasteland",friend));}
 @Test void oldSnapshotsAndDisconnectNeverPreserveTeammate(){var friend=UUID.randomUUID();var s=new MatchNameTagState();s.receive(new MatchMapSnapshot(1,1,UUID.randomUUID(),"exodus:wasteland",0,0,2000,false,List.of(),Set.of(friend)));s.reset();assertFalse(s.teammate("exodus:wasteland",friend));assertFalse(s.hidden("exodus:wasteland",true,false,friend));}
 @Test void snapshotCannotCarryMultipleTeammatesOrPrivateDataOnClear(){assertThrows(IllegalArgumentException.class,()->new MatchMapSnapshot(1,1,UUID.randomUUID(),"exodus:wasteland",0,0,2000,false,List.of(),Set.of(UUID.randomUUID(),UUID.randomUUID())));assertThrows(IllegalArgumentException.class,()->new MatchMapSnapshot(1,1,null,"",0,0,0,true,List.of(),Set.of(UUID.randomUUID())));}
 @Test void privateMarkerRequiresBothParticipantsInMatchDimension(){assertTrue(TeammateProjectionPolicy.visible(true,true,true,true));assertFalse(TeammateProjectionPolicy.visible(false,true,true,true));assertFalse(TeammateProjectionPolicy.visible(true,false,true,true));assertFalse(TeammateProjectionPolicy.visible(true,true,false,true));assertFalse(TeammateProjectionPolicy.visible(true,true,true,false));}
}
