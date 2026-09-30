package dev.exodus.enemy;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EnemyPolicyTest {
    private final EnemyPressurePolicy.Settings settings = new EnemyPressurePolicy.Settings(30, 200, 12, 24, 2, 4);
    @Test void deviceReplacesOrdinaryTargetAndRate() {
        assertEquals(new EnemyPressurePolicy.Pressure(12, 2), EnemyPressurePolicy.evaluate(true, false, 0, settings));
        assertEquals(new EnemyPressurePolicy.Pressure(24, 2), EnemyPressurePolicy.evaluate(false, false, 0, settings));
        assertEquals(new EnemyPressurePolicy.Pressure(27, 4), EnemyPressurePolicy.evaluate(true, true, 3, settings));
        assertEquals(new EnemyPressurePolicy.Pressure(0, 4), EnemyPressurePolicy.evaluate(false, true, 30, settings));
    }
    @Test void nearbyPlayersRetainIndependentAllocationsAndGlobalCap() {
        var population = new EnemyPopulation();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        for (int i = 0; i < 30; i++) population.add(UUID.randomUUID(), first, EnemyKind.ZOMBIE);
        assertEquals(0, population.remaining(first, 30, 200));
        assertEquals(30, population.remaining(second, 30, 200));
        for (int i = 0; i < 169; i++) population.add(UUID.randomUUID(), UUID.randomUUID(), EnemyKind.ZOMBIE);
        assertEquals(1, population.remaining(second, 30, 200));
        assertFalse(population.canAdd(second, 3, 30, 200));
    }
    @Test void scheduleNeverBackfillsOrRepeatsFailedOpportunities() {
        var schedule = new SoldierSchedule(6000, 24000);
        UUID owner = UUID.randomUUID();
        assertNull(schedule.due(owner, 4000, 1000));
        assertNull(schedule.due(owner, 6999, 1000));
        assertNotNull(schedule.due(owner, 7000, 1000));
        assertNull(schedule.due(owner, 7001, 1000));
        assertNull(schedule.due(owner, 13000, 1000));
        assertNull(schedule.due(owner, 24000, 1000));
        assertNotNull(schedule.due(owner, 25000, 1000));
        assertNull(schedule.due(owner, 25001, 1000));
        assertNull(schedule.due(owner, 30999, 1000));
        assertNotNull(schedule.due(owner, 31000, 1000));
    }
    @Test void firstSeenAfterOpportunitySkipsIt() {
        var schedule = new SoldierSchedule(6000, 24000);
        var owner = UUID.randomUUID();
        assertNull(schedule.due(owner, 3000, 1000));
        assertNull(schedule.due(owner, 3001, 1000));
        assertNull(schedule.due(owner, 6999, 1000));
        assertNotNull(schedule.due(owner, 7000, 1000));
    }
    @Test void reconnectCannotBackfillAnOpportunity() {
        var schedule=new SoldierSchedule(6000,24000);
        UUID owner=UUID.randomUUID();
        assertNull(schedule.due(owner,100,1000));
        assertNull(schedule.due(owner,3000,1000));
        assertNull(schedule.due(owner,3001,1000));
        assertNull(schedule.due(owner,6999,1000));
        assertNotNull(schedule.due(owner,7000,1000));
    }
    @Test void combatHysteresisAppliesOnlyToSelectedRelationship() {
        assertTrue(EnemyRangePolicy.allowed(10 * 10, false, 10, 16));
        assertFalse(EnemyRangePolicy.allowed(11 * 11, false, 10, 16));
        assertTrue(EnemyRangePolicy.allowed(16 * 16, true, 10, 16));
        assertFalse(EnemyRangePolicy.allowed(17 * 17, true, 10, 16));
        assertTrue(EnemyRangePolicy.allowed(48 * 48, true, 32, 48));
    }
    @Test void admissionRejectsStaleManagedMobsEvenWithAdminTag() {
        assertFalse(EnemyAdmission.allowed(true, false, true));
        assertTrue(EnemyAdmission.allowed(true, true, false));
        assertTrue(EnemyAdmission.allowed(false, false, true));
        assertFalse(EnemyAdmission.allowed(false, false, false));
    }
    @Test void zombieLootRollsAreIndependentAndBounded() {
        assertEquals(new ZombieLootPolicy.Drops(1, 1, 1), ZombieLootPolicy.roll(0, 0, 0, .01, .03, .03));
        assertEquals(new ZombieLootPolicy.Drops(0, 0, 0), ZombieLootPolicy.roll(.01, .03, .03, .01, .03, .03));
        assertEquals(new ZombieLootPolicy.Drops(0, 1, 0), ZombieLootPolicy.roll(.5, .02, .5, .01, .03, .03));
    }
    @Test void cleanupRequiresDistanceAndNoCombat() {
        assertFalse(EnemyRangePolicy.farForCleanup(128*128,128,false));
        assertTrue(EnemyRangePolicy.farForCleanup(129*129,128,false));
        assertFalse(EnemyRangePolicy.farForCleanup(500*500,128,true));
    }
    @Test void populationRejoinDoesNotDoubleCountAndRemovalReleasesCapacity() {
        var population=new EnemyPopulation();
        UUID entity=UUID.randomUUID(),owner=UUID.randomUUID();
        population.add(entity,owner,EnemyKind.RUSSIAN);
        population.add(entity,owner,EnemyKind.RUSSIAN);
        assertEquals(1,population.size());
        assertEquals(0,population.zombies(owner));
        population.remove(entity);
        assertEquals(30,population.remaining(owner,30,200));
    }
}
