package dev.exodus.wasteland.placement;

import dev.exodus.wasteland.profile.FixedCityLayout;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedCityPlacementTest {
    @Test void newLayoutPoisStayOutsideCityForSeveralSeeds() {
        var layout = FixedCityLayout.load();
        for (long seed = 0; seed < 10; seed++) {
            var plan = PlacementPolicy.plan(seed,4096,0,2000,10,5,120,
                    List.of("exodus:abandoned_camp_01"),List.of("exodus:occupied_camp_01"),layout);
            assertEquals(17, plan.entries().size());
            for (var entry : plan.entries()) {
                assertFalse(layout.intersectsCity(4096,0,entry.x(),entry.z(),entry.maxX(),entry.maxZ(),layout.poiMargin()));
                assertTrue(entry.x() >= 3096 && entry.maxX() < 5096);
                assertTrue(entry.z() >= -1000 && entry.maxZ() < 1000);
            }
        }
    }
}
