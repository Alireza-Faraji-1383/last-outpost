package dev.exodus.event;

import dev.exodus.event.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EventPolicyTest {
    private EventDefinition hunt(boolean once, int cooldown) {
        return new EventDefinition("exodus:hunt", "Zombie Hunt", EventDefinition.Scope.GLOBAL,
                EventDefinition.Objective.KILL_ENTITY, 1, 10, 40, once, cooldown, 0, "", false);
    }
    @Test void dayWindowsAndCooldownExcludeIneligibleDefinitions() {
        var s = new EventSchedule(); var d = hunt(false, 2);
        assertFalse(s.eligible(d, 0)); assertTrue(s.eligible(d, 1));
        s.started(d, 1); assertFalse(s.eligible(d, 2)); assertTrue(s.eligible(d, 3));
        assertFalse(s.eligible(d, 11));
    }
    @Test void oneTimeRemainsConsumedAfterCooldown() {
        var s = new EventSchedule(); var d = hunt(true, 1); s.started(d, 1);
        assertFalse(s.eligible(d, 9));
    }
    @Test void failedMilestonePlacementDoesNotConsumeGuaranteedDrop() {
        var d = new EventDefinition("exodus:core", "Core", EventDefinition.Scope.GLOBAL,
                EventDefinition.Objective.WORLD_DROP, 5, 0, 0, true, 0, 5, "exodus:events/equipment", true);
        var s = new EventSchedule();
        assertTrue(s.milestones(List.of(d), 7).contains(d));
        s.started(d, 7); assertTrue(s.milestones(List.of(d), 8).isEmpty());
        assertTrue(s.random(List.of(d), 5, new Random(1)).isEmpty());
    }
    @Test void dailyRollIsOnceAndDoesNotDependOnWorldClock() {
        var s = new EventSchedule(); assertTrue(s.rollDay(1)); assertFalse(s.rollDay(1));
        assertTrue(s.rollDay(5)); assertFalse(s.rollDay(4));
        assertEquals(5, EventSchedule.day(96000, 24000));
    }
    @Test void weightedSelectionOnlyReturnsEligiblePositiveWeightEvents() {
        var s = new EventSchedule(); var d = hunt(false, 0);
        assertEquals(d, s.random(List.of(d), 1, new Random(2)).orElseThrow());
        assertTrue(s.random(List.of(d), 20, new Random(2)).isEmpty());
    }
    @Test void zombieKillsCountOnceAndOnlyBeforeDeadline() {
        UUID p = UUID.randomUUID(), victim = UUID.randomUUID();
        var run = ObjectiveProgress.hunt(Set.of(p), 100, 1800, 2);
        assertFalse(run.kill(UUID.randomUUID(), victim, 101));
        assertTrue(run.kill(p, victim, 101)); assertFalse(run.kill(p, victim, 102));
        assertFalse(run.kill(p, UUID.randomUUID(), 1900));
        assertEquals(1, run.kills()); assertEquals(ObjectiveProgress.Outcome.ACTIVE, run.outcome());
        run.advance(1900); assertEquals(ObjectiveProgress.Outcome.FAILED, run.outcome());
    }
    @Test void cooperativeSuccessRewardsOnlyContributors() {
        UUID p=UUID.randomUUID(), q=UUID.randomUUID();
        var run=ObjectiveProgress.hunt(Set.of(p,q),0,1800,2);
        run.kill(p,UUID.randomUUID(),1); run.kill(p,UUID.randomUUID(),2);
        assertEquals(ObjectiveProgress.Outcome.COMPLETED,run.outcome());
        assertEquals(Set.of(p),run.winners());
        assertTrue(run.claimReward(p)); assertFalse(run.claimReward(p)); assertFalse(run.claimReward(q));
    }
    @Test void preyDeathFromAnyCauseWinsBeforeSurvivalTimeout() {
        UUID hunter=UUID.randomUUID(), prey=UUID.randomUUID();
        var run=ObjectiveProgress.manhunt(hunter,prey,0,6000);
        run.death(prey,6000); run.advance(6000);
        assertEquals(ObjectiveProgress.Outcome.HUNTER_WON,run.outcome()); assertEquals(Set.of(hunter),run.winners());
        assertTrue(run.claimReward(hunter)); assertFalse(run.claimReward(hunter));
    }
    @Test void survivalTimeoutPaysPreyAndCannotBeReversed() {
        UUID hunter=UUID.randomUUID(),prey=UUID.randomUUID();
        var run=ObjectiveProgress.manhunt(hunter,prey,100,6000); run.advance(6100); run.death(prey,6101);
        assertEquals(ObjectiveProgress.Outcome.PREY_WON,run.outcome());assertEquals(Set.of(prey),run.winners());
    }
    @Test void hunterDeathCancelsAndSamePlayerCannotHaveBothRoles() {
        UUID hunter=UUID.randomUUID(),prey=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,()->ObjectiveProgress.manhunt(hunter,hunter,0,6000));
        var run=ObjectiveProgress.manhunt(hunter,prey,0,6000);run.death(hunter,1);
        assertEquals(ObjectiveProgress.Outcome.CANCELLED,run.outcome());assertTrue(run.winners().isEmpty());
    }
    @Test void terminalEventsIgnoreFurtherKillsAndCancellation() {
        UUID p=UUID.randomUUID();var run=ObjectiveProgress.hunt(Set.of(p),0,1800,1);
        run.kill(p,UUID.randomUUID(),1);run.cancel();
        assertFalse(run.kill(p,UUID.randomUUID(),2));assertEquals(1,run.kills());
        assertEquals(ObjectiveProgress.Outcome.COMPLETED,run.outcome());
    }
    @Test void invalidDefinitionCombinationsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new EventDefinition("x","x",EventDefinition.Scope.GLOBAL,
                EventDefinition.Objective.KILL_PLAYER,1,0,1,false,0,0,"",false));
    }
}
