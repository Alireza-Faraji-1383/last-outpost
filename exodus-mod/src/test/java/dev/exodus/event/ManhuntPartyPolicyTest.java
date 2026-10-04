package dev.exodus.event;
import dev.exodus.event.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ManhuntPartyPolicyTest {
 private final UUID a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID();
 @Test void pairSelectionExcludesExistingTeammates(){var pairs=ManhuntPartyPolicy.pairs(List.of(a,b,c),(x,y)->Set.of(a,b).contains(x)&&Set.of(a,b).contains(y));assertEquals(4,pairs.size());assertTrue(pairs.stream().noneMatch(p->Set.of(a,b).equals(Set.of(p.hunter(),p.prey()))));}
 @Test void twoAlliedPlayersHaveNoValidManhunt(){assertTrue(ManhuntPartyPolicy.pairs(List.of(a,b),(x,y)->true).isEmpty());}
 @Test void allianceFailsBeforeDeathOrTimeoutAndNeverPays(){var p=ObjectiveProgress.manhunt(a,b,0,600);p.failAlliance();p.death(b,599);p.advance(600);assertEquals(ObjectiveProgress.Outcome.FAILED,p.outcome());assertTrue(p.winners().isEmpty());assertFalse(p.claimReward(a));assertFalse(p.claimReward(b));}
 @Test void allianceCannotAlterUnrelatedCompletedOutcome(){var p=ObjectiveProgress.manhunt(a,b,0,600);p.death(b,1);p.failAlliance();assertEquals(ObjectiveProgress.Outcome.HUNTER_WON,p.outcome());}
}
