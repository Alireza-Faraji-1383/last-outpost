package dev.exodus.supply.domain;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RadioCooldownTest {
    @Test void changingSupplyCannotBypassCooldownAndNewMatchResetsIt(){
        UUID match=UUID.randomUUID();
        var usages=List.of(new RadioUsage(match,1,1800),new RadioUsage(match,2,600));
        assertEquals(1800,RadioUsagePolicy.cooldownUntil(usages,match));
        assertEquals(0,RadioUsagePolicy.cooldownUntil(usages,UUID.randomUUID()));
        assertEquals(0,RadioUsagePolicy.cooldownUntil(List.of(),match));
    }
}
