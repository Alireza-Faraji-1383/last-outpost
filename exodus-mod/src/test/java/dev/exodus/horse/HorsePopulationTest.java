package dev.exodus.horse;

import dev.exodus.enemy.SoldierSchedule;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HorsePopulationTest {
    @Test void unloadedHorsesStillCountAndDeathFreesCapacity() {
        var population=new HorsePopulation();
        UUID first=UUID.randomUUID();
        population.add(first);
        for(int i=0;i<9;i++)population.add(UUID.randomUUID());
        assertEquals(0,population.remaining(10));
        population.add(first);
        assertEquals(0,population.remaining(10));
        population.remove(first);
        assertEquals(1,population.remaining(10));
        population.clear();assertEquals(10,population.remaining(10));
    }
    @Test void globalScheduleGivesExactlyTwoDaytimeOpportunitiesWithoutCatchup() {
        var schedule=new SoldierSchedule(6000,24000);
        UUID match=UUID.randomUUID();int opportunities=0;
        for(int time=0;time<48000;time++)if(schedule.due(match,time,1000)!=null)opportunities++;
        assertEquals(4,opportunities);
        assertNull(schedule.due(match,47000,1000));
        schedule.clear();assertNull(schedule.due(match,4000,1000));
        assertNotNull(schedule.due(match,7000,1000));
        assertNull(schedule.due(match,7000,1000));
        assertNull(schedule.due(match,31001,1000));
    }
}
