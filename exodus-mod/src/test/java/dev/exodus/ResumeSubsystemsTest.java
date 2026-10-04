package dev.exodus;
import dev.exodus.party.PartyState;
import dev.exodus.event.EventSavedState;
import dev.exodus.event.EventManager;
import dev.exodus.event.domain.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ResumeSubsystemsTest {
 @Test void partyDepartureAndInvitationSurviveSave(){
  UUID a=UUID.randomUUID(),b=UUID.randomUUID();var p=new PartyState(UUID.randomUUID(),2);p.create(a);p.invite(a,b,10,100);
  var r=PartyState.load(p.save());r.accept(b,a,20);r.leave(a,30,600);
  r=PartyState.load(r.save());assertTrue(r.sameParty(a,b));assertEquals(630,r.departureDeadline(a));assertTrue(r.tick(629).isEmpty());assertEquals(1,r.tick(630).size());
 }
 @Test void huntProgressAndDuplicateVictimsSurviveSave(){
  UUID player=UUID.randomUUID(),victim=UUID.randomUUID();var p=ObjectiveProgress.hunt(Set.of(player),100,1000,2);p.kill(player,victim,101);
  var def=new EventDefinition("exodus:test","Test",EventDefinition.Scope.GLOBAL,EventDefinition.Objective.KILL_ENTITY,1,0,1,false,1,0,"",false);
  var s=new EventSavedState();s.runs.add(new EventManager.Run(UUID.randomUUID(),def,p,10,20,30));
  var r=EventSavedState.load(s.save()).runs.get(0);assertEquals(1,r.progress().kills());assertEquals(900,r.progress().remaining(200));assertFalse(r.progress().kill(player,victim,201));assertTrue(r.progress().kill(player,UUID.randomUUID(),202));assertTrue(r.progress().claimReward(player));assertFalse(r.progress().claimReward(player));assertEquals(10,r.huntReward());
 }
 @Test void manhuntOutcomeAndPaidRewardCannotRepeatAfterReload(){
  UUID hunter=UUID.randomUUID(),prey=UUID.randomUUID();var p=ObjectiveProgress.manhunt(hunter,prey,100,1000);
  var restored=ObjectiveProgress.load(p.save());restored.death(prey,200);assertEquals(ObjectiveProgress.Outcome.HUNTER_WON,restored.outcome());assertTrue(restored.claimReward(hunter));
  restored=ObjectiveProgress.load(restored.save());assertEquals(Set.of(hunter),restored.winners());assertFalse(restored.claimReward(hunter));restored.advance(2000);assertEquals(ObjectiveProgress.Outcome.HUNTER_WON,restored.outcome());
 }
}
