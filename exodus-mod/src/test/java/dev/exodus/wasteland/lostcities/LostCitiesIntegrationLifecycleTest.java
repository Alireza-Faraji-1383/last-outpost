package dev.exodus.wasteland.lostcities;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCityInformation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LostCitiesIntegrationLifecycleTest {
    @Test
    void defersDimensionRegistrationUntilServerConfigsAreLoaded() {
        RecordingApi api = new RecordingApi();
        LostCitiesLifecycle lifecycle = new LostCitiesLifecycle();

        lifecycle.acceptApi(api);

        assertEquals(0, api.registrations);

        lifecycle.registerDimension(null, "exodus");

        assertEquals(1, api.registrations);
        assertEquals(null, api.dimension);
        assertEquals("exodus", api.profile);
    }

    private static final class RecordingApi implements ILostCities {
        private int registrations;
        private ResourceKey<Level> dimension;
        private String profile;

        @Override public ILostCityInformation getLostInfo(Level level) { return null; }
        @Override public void registerDimension(ResourceKey<Level> dimension, String profile) {
            registrations++;
            this.dimension = dimension;
            this.profile = profile;
        }
        @Override public void setOverworldProfile(String profile) {}
    }
}
